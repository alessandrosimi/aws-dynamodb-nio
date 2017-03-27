package com.amazonaws.http

import java.io.IOException
import java.net.{InetSocketAddress, Socket, UnknownHostException}
import java.security.NoSuchAlgorithmException
import java.security.cert.{CertificateException, X509Certificate}
import javax.net.ssl.{SSLContext, SSLSocket, TrustManager, X509TrustManager}

import com.amazonaws.http.ClientBuilder._
import com.amazonaws.http.conn.ssl.SdkTLSSocketFactory
import com.amazonaws.http.impl.client.{HttpRequestNoRetryHandler, SdkHttpClient}
import com.amazonaws.{AmazonClientException, ClientConfiguration, SDKGlobalConfiguration}
import org.apache.http.auth.{AuthScope, ChallengeState, NTCredentials}
import org.apache.http.client.config.RequestConfig
import org.apache.http.client.entity.GzipDecompressingEntity
import org.apache.http.client.methods.HttpUriRequest
import org.apache.http.client.protocol.ClientContext
import org.apache.http.client.{AuthCache, HttpClient, RedirectStrategy}
import org.apache.http.config.RegistryBuilder
import org.apache.http.conn.ConnectTimeoutException
import org.apache.http.conn.params.{ConnRoutePNames, ConnRouteParams}
import org.apache.http.conn.scheme._
import org.apache.http.conn.ssl.SSLSocketFactory
import org.apache.http.impl.auth.BasicScheme
import org.apache.http.impl.client.BasicAuthCache
import org.apache.http.impl.conn.PoolingClientConnectionManager
import org.apache.http.impl.nio.client.{HttpAsyncClientBuilder, CloseableHttpAsyncClient}
import org.apache.http.impl.nio.conn.PoolingNHttpClientConnectionManager
import org.apache.http.impl.nio.reactor.{DefaultConnectingIOReactor, IOReactorConfig}
import org.apache.http.nio.conn.ssl.SSLIOSessionStrategy
import org.apache.http.nio.conn.{NoopIOSessionStrategy, SchemeIOSessionStrategy}
import org.apache.http.params.{BasicHttpParams, HttpConnectionParams, HttpParams}
import org.apache.http.protocol.HttpContext
import org.apache.http.{HttpResponse => ApacheHttpResponse, _}

class ClientBuilder(config: ClientConfiguration) {

  def build: CloseableHttpAsyncClient = { // TODO SSL and Proxy
    // Request Config
    val requestConfig = RequestConfig.custom()
      .setConnectTimeout(config.getConnectionTimeout)
      .setSocketTimeout(config.getSocketTimeout)
      .setStaleConnectionCheckEnabled(true)
      .setLocalAddress(config.getLocalAddress)
      .build()
    // IO Reactor Config
    val ioReactorConfigBuilder = IOReactorConfig.custom()
      .setTcpNoDelay(true)
      .setSoKeepAlive(config.useTcpKeepAlive)
      .setSoTimeout(config.getSocketTimeout)
    val sendBufferSize = config.getSocketBufferSizeHints.apply(0)
    if (sendBufferSize > 0) ioReactorConfigBuilder.setSndBufSize(sendBufferSize)
    val receiveBufferSize = config.getSocketBufferSizeHints.apply(1)
    if (receiveBufferSize > 0) ioReactorConfigBuilder.setRcvBufSize(receiveBufferSize)
    val ioReactorConfig = ioReactorConfigBuilder.build()
    // Connection Manager
    val sslStrategy = new SSLIOSessionStrategy(SSLContext.getDefault)
    val registry = RegistryBuilder
      .create[SchemeIOSessionStrategy]
      .register("http", NoopIOSessionStrategy.INSTANCE)
      .register("https", sslStrategy).build
    val ioReactor = new DefaultConnectingIOReactor(ioReactorConfig)
    val connectionManager = new PoolingNHttpClientConnectionManager(ioReactor, registry)
    // Client
    val clientBuilder = HttpAsyncClientBuilder
      .create()
      .setDefaultRequestConfig(requestConfig)
      .setDefaultIOReactorConfig(ioReactorConfig)
      //.setConnectionManager(connectionManager)
      .setRedirectStrategy(NeverFollowRedirectStrategy)
    if (config.useGzip()) {
      clientBuilder
        .addInterceptorLast(GzipRequestInterceptor)
        .addInterceptorLast(GzipResponseInterceptor)
    }
    clientBuilder.build()
  }

  /**
    * Creates a new HttpClient object using the specified AWS
    * ClientConfiguration to configure the client.
    *
    * @param config
    * Client configuration options (ex: proxy settings, connection
    * limits, etc).
    * @return The new, configured HttpClient.
    */
  private def createHttpClient(config: ClientConfiguration): HttpClient = {
    /* Set HTTP client parameters */
    val httpClientParams: HttpParams = new BasicHttpParams
    HttpConnectionParams.setConnectionTimeout(httpClientParams, config.getConnectionTimeout) // Done
    HttpConnectionParams.setSoTimeout(httpClientParams, config.getSocketTimeout) // Done
    HttpConnectionParams.setStaleCheckingEnabled(httpClientParams, true) // Done
    HttpConnectionParams.setTcpNoDelay(httpClientParams, true) // Done
    HttpConnectionParams.setSoKeepalive(httpClientParams, config.useTcpKeepAlive) // Done
    val socketSendBufferSizeHint: Int = config.getSocketBufferSizeHints.apply(0)
    val socketReceiveBufferSizeHint: Int = config.getSocketBufferSizeHints.apply(1)
    if (socketSendBufferSizeHint > 0 || socketReceiveBufferSizeHint > 0) HttpConnectionParams.setSocketBufferSize(httpClientParams, Math.max(socketSendBufferSizeHint, socketReceiveBufferSizeHint)) // Done
    val connectionManager: PoolingClientConnectionManager = ConnectionManagerFactory.createPoolingClientConnManager(config, httpClientParams)
    val httpClient: SdkHttpClient = new SdkHttpClient(connectionManager, httpClientParams)
    httpClient.setHttpRequestRetryHandler(HttpRequestNoRetryHandler.Singleton) // Not Applicable
    httpClient.setRedirectStrategy(NeverFollowRedirectStrategy) // Done
    if (config.getLocalAddress != null) ConnRouteParams.setLocalAddress(httpClientParams, config.getLocalAddress) // Done
    try {
      val http: Scheme = new Scheme("http", 80, PlainSocketFactory.getSocketFactory)
      var sf: SSLSocketFactory = config.getApacheHttpClientConfig.getSslSocketFactory
      if (sf == null) sf = new SdkTLSSocketFactory(SSLContext.getDefault, SSLSocketFactory.STRICT_HOSTNAME_VERIFIER)
      val https: Scheme = new Scheme("https", 443, sf)
      val sr: SchemeRegistry = connectionManager.getSchemeRegistry
      sr.register(http)
      sr.register(https)
    } catch {
      case e: NoSuchAlgorithmException => {
        throw new AmazonClientException("Unable to access default SSL context", e)
      }
    }
    /*
     * If SSL cert checking for endpoints has been explicitly disabled,
     * register a new scheme for HTTPS that won't cause self-signed certs to
     * error out.
     */
    if (System.getProperty(SDKGlobalConfiguration.DISABLE_CERT_CHECKING_SYSTEM_PROPERTY) != null) {
      val sch: Scheme = new Scheme("https", 443, new ClientBuilder.TrustingSocketFactory)
      httpClient.getConnectionManager.getSchemeRegistry.register(sch)
    }
    /* Set proxy if configured */
    val proxyHost: String = config.getProxyHost
    val proxyPort: Int = config.getProxyPort
    if (proxyHost != null && proxyPort > 0) {
      AmazonHttpClient.log.info("Configuring Proxy. Proxy Host: " + proxyHost + " " + "Proxy Port: " + proxyPort)
      val proxyHttpHost: HttpHost = new HttpHost(proxyHost, proxyPort)
      httpClient.getParams.setParameter(ConnRoutePNames.DEFAULT_PROXY, proxyHttpHost)
      val proxyUsername: String = config.getProxyUsername
      val proxyPassword: String = config.getProxyPassword
      val proxyDomain: String = config.getProxyDomain
      val proxyWorkstation: String = config.getProxyWorkstation
      if (proxyUsername != null && proxyPassword != null) httpClient.getCredentialsProvider.setCredentials(new AuthScope(proxyHost, proxyPort), new NTCredentials(proxyUsername, proxyPassword, proxyWorkstation, proxyDomain))
      // Add a request interceptor that sets up proxy authentication pre-emptively if configured
      if (config.isPreemptiveBasicProxyAuth) httpClient.addRequestInterceptor(new ClientBuilder.PreemptiveProxyAuth(proxyHttpHost), 0)
    }
    /* Accept Gzip response if configured */
    if (config.useGzip) {
      httpClient.addRequestInterceptor(new HttpRequestInterceptor() { // DOne
      @throws[HttpException]
      @throws[IOException]
      def process(request: HttpRequest, context: HttpContext) {
        if (!request.containsHeader("Accept-Encoding")) request.addHeader("Accept-Encoding", "gzip")
      }
      })
      httpClient.addResponseInterceptor(new HttpResponseInterceptor() { // Done
      @throws[HttpException]
      @throws[IOException]
      def process(response: ApacheHttpResponse, context: HttpContext) {
        val entity: HttpEntity = response.getEntity
        if (entity != null) {
          val ceheader: Header = entity.getContentEncoding
          if (ceheader != null) {
            val codecs: Array[HeaderElement] = ceheader.getElements
            var i: Int = 0
            while (i < codecs.length) {
              {
                if (codecs(i).getName.equalsIgnoreCase("gzip")) {
                  response.setEntity(new GzipDecompressingEntity(response.getEntity))
                  return
                }
              }
              {
                i += 1; i - 1
              }
            }
          }
        }
      }
      })
    }
    httpClient
  }

}

object ClientBuilder {

  /**
    * Disable http redirect inside Apache HttpClient.
    */
  private object NeverFollowRedirectStrategy extends RedirectStrategy {
    def isRedirected(request: HttpRequest, response: ApacheHttpResponse, context: HttpContext): Boolean = false
    def getRedirect(request: HttpRequest, response: ApacheHttpResponse, context: HttpContext): HttpUriRequest = null
  }



  private val GzipRequestInterceptor = new HttpRequestInterceptor() {
    def process(request: HttpRequest, context: HttpContext) {
      if (!request.containsHeader("Accept-Encoding")) request.addHeader("Accept-Encoding", "gzip")
    }
  }

  private val GzipResponseInterceptor = new HttpResponseInterceptor() {
    def process(response: ApacheHttpResponse, context: HttpContext) {
      val hasGzipCodec = Option(response.getEntity)
        .flatMap(entity => Option(entity.getContentEncoding))
        .map(contentEncoding => contentEncoding.getElements)
        .exists(codecs => codecs.exists(codec => codec.getName.equalsIgnoreCase("gzip")))
      if (hasGzipCodec) response.setEntity(new GzipDecompressingEntity(response.getEntity))
    }
  }

  /**
    * Simple implementation of SchemeSocketFactory (and
    * LayeredSchemeSocketFactory) that bypasses SSL certificate checks. This
    * class is only intended to be used for testing purposes.
    */
  private object TrustingSocketFactory {
    @throws[IOException]
    private def createSSLContext: SSLContext = try {
      val context: SSLContext = SSLContext.getInstance("TLS")
      context.init(null, Array[TrustManager](new TrustingX509TrustManager), null)
      context
    } catch {
      case e: Exception => {
        throw new IOException(e.getMessage, e)
      }
    }
  }

  private class TrustingSocketFactory extends SchemeSocketFactory with SchemeLayeredSocketFactory {
    private var sslcontext: SSLContext = null

    @throws[IOException]
    private def getSSLContext: SSLContext = {
      if (this.sslcontext == null) this.sslcontext = TrustingSocketFactory.createSSLContext
      this.sslcontext
    }

    @throws[IOException]
    def createSocket(params: HttpParams): Socket = getSSLContext.getSocketFactory.createSocket

    @throws[IOException]
    @throws[UnknownHostException]
    @throws[ConnectTimeoutException]
    def connectSocket(sock: Socket, remoteAddress: InetSocketAddress, localAddress: InetSocketAddress, params: HttpParams): Socket = {
      val connTimeout: Int = HttpConnectionParams.getConnectionTimeout(params)
      val soTimeout: Int = HttpConnectionParams.getSoTimeout(params)
      val sslsock: SSLSocket = (if (sock != null) sock
      else createSocket(params)).asInstanceOf[SSLSocket]
      if (localAddress != null) sslsock.bind(localAddress)
      sslsock.connect(remoteAddress, connTimeout)
      sslsock.setSoTimeout(soTimeout)
      sslsock
    }

    @throws[IllegalArgumentException]
    def isSecure(sock: Socket): Boolean = true

    @throws[IOException]
    @throws[UnknownHostException]
    def createLayeredSocket(arg0: Socket, arg1: String, arg2: Int, arg3: HttpParams): Socket = getSSLContext.getSocketFactory.createSocket(arg0, arg1, arg2, true)
  }

  /**
    * Simple implementation of X509TrustManager that trusts all certificates.
    * This class is only intended to be used for testing purposes.
    */
  private object TrustingX509TrustManager {
    private val X509_CERTIFICATES: Array[X509Certificate] = new Array[X509Certificate](0)
  }

  private class TrustingX509TrustManager extends X509TrustManager {
    def getAcceptedIssuers: Array[X509Certificate] = TrustingX509TrustManager.X509_CERTIFICATES

    @throws[CertificateException]
    def checkServerTrusted(chain: Array[X509Certificate], authType: String) {
      // No-op, to trust all certs
    }

    @throws[CertificateException]
    def checkClientTrusted(chain: Array[X509Certificate], authType: String) {
      // No-op, to trust all certs
    }
  }

  /**
    * HttpRequestInterceptor implementation to set up pre-emptive
    * authentication against a defined basic proxy server.
    */
  private class PreemptiveProxyAuth(val proxyHost: HttpHost) extends HttpRequestInterceptor {
    def process(request: HttpRequest, context: HttpContext) {
      var authCache: AuthCache = null
      // Set up the a Basic Auth scheme scoped for the proxy - we don't
      // want to do this for non-proxy authentication.
      val basicScheme: BasicScheme = new BasicScheme(ChallengeState.PROXY)
      if (context.getAttribute(ClientContext.AUTH_CACHE) == null) {
        authCache = new BasicAuthCache
        authCache.put(this.proxyHost, basicScheme)
        context.setAttribute(ClientContext.AUTH_CACHE, authCache)
      }
      else {
        authCache = context.getAttribute(ClientContext.AUTH_CACHE).asInstanceOf[AuthCache]
        authCache.put(this.proxyHost, basicScheme)
      }
    }
  }

}

