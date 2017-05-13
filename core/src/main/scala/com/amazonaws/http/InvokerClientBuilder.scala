/**
  * Copyright 2017 Alessandro Simi
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
package com.amazonaws.http

import javax.net.ssl.SSLContext

import com.amazonaws.ClientConfiguration
import com.amazonaws.http.InvokerClientBuilder._
import org.apache.http.client.RedirectStrategy
import org.apache.http.client.config.RequestConfig
import org.apache.http.client.entity.GzipDecompressingEntity
import org.apache.http.client.methods.HttpUriRequest
import org.apache.http.config.RegistryBuilder
import org.apache.http.conn.ssl.SSLSocketFactory
import org.apache.http.impl.nio.client.{CloseableHttpAsyncClient, HttpAsyncClientBuilder}
import org.apache.http.impl.nio.conn.PoolingNHttpClientConnectionManager
import org.apache.http.impl.nio.reactor.{DefaultConnectingIOReactor, IOReactorConfig}
import org.apache.http.nio.conn.ssl.SSLIOSessionStrategy
import org.apache.http.nio.conn.{NoopIOSessionStrategy, SchemeIOSessionStrategy}
import org.apache.http.protocol.HttpContext
import org.apache.http.{HttpResponse => ApacheHttpResponse, _}

private[http] class InvokerClientBuilder(config: ClientConfiguration) {

  def build: CloseableHttpAsyncClient = {
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
    val sslStrategy = new SSLIOSessionStrategy(SSLContext.getDefault, SSLSocketFactory.STRICT_HOSTNAME_VERIFIER)
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
      .setConnectionManager(connectionManager)
      .setRedirectStrategy(NeverFollowRedirectStrategy)
    if (config.useGzip()) {
      clientBuilder
        .addInterceptorLast(GzipRequestInterceptor)
        .addInterceptorLast(GzipResponseInterceptor)
    }
    clientBuilder.build()
  }

}

private object InvokerClientBuilder {

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

}

