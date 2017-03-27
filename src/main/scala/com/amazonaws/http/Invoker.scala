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


import java.io.IOException
import java.net.URI

import com.amazonaws.AmazonServiceException.ErrorType
import com.amazonaws._
import com.amazonaws.auth.{AWS4Signer, AWSCredentialsProvider}
import com.amazonaws.event.ProgressInputStream
import com.amazonaws.transform.{JsonUnmarshallerContext, Marshaller, Unmarshaller}
import com.amazonaws.util.AwsHostNameUtils
import org.apache.http.client.methods.{HttpRequestBase, HttpUriRequest}
import org.apache.http.concurrent.FutureCallback
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient
import org.apache.http.{HttpStatus, HttpResponse => ApacheHttpResponse}

import scala.concurrent.{Future, Promise}
import scala.util.{Failure, Success, Try}

class Invoker(serviceName: String,
              endpoint: URI,
              awsCredentialsProvider: AWSCredentialsProvider,
              config: ClientConfiguration,
              executionContext: concurrent.ExecutionContext,
              errorResponseHandler: HttpResponseHandler[AmazonServiceException]) {

  private implicit val ec = executionContext

  private val client = new ClientBuilder(config).build

  private val httpRequestFactory = new HttpRequestFactory

  private val signer = {
    val signer = new AWS4Signer
    signer.setServiceName(serviceName)
    val region = AwsHostNameUtils.parseRegionName(endpoint.getHost, serviceName)
    signer.setRegionName(region)
    signer
  }

  def start(): Invoker = {
    client.start()
    this
  }

  def stop() = client.close()

  def invoke[Req, Resp](req: Req,
                        marshaller: Marshaller[Request[Req], Req],
                        unmarshaller: Unmarshaller[Resp, JsonUnmarshallerContext]): Future[Resp] = {
    // Request
    val request = marshaller.marshall(req)
    request.setEndpoint(endpoint)
    // Auth
    val credentials = awsCredentialsProvider.getCredentials
    signer.sign(request, credentials)
    // Apache Request
    val apacheHttpRequest = httpRequestFactory.createHttpRequest(request, null, null) // With two values set to null is not possible to set a custom user agent
    // Execute Request
    val futureApacheResponse = processRequest(client, apacheHttpRequest)
    // Response
    futureApacheResponse.map {
      case apacheHttpResponse if isRequestSuccessful(apacheHttpResponse) =>
        val httpResponse = createResponse(apacheHttpRequest, request, apacheHttpResponse)
        val response = handleResponse(
          request = request,
          responseHandler = new JsonResponseHandler[Resp](unmarshaller),
          method = apacheHttpRequest,
          httpResponse = httpResponse,
          apacheHttpResponse = apacheHttpResponse
        )
        new Response[Resp](response, httpResponse)
      case apacheHttpResponse if isTemporaryRedirect(apacheHttpResponse) =>
        ???
      case apacheHttpResponse =>
        val error = handleErrorResponse(
          request = request,
          errorResponseHandler = errorResponseHandler,
          method = apacheHttpRequest,
          apacheHttpResponse = apacheHttpResponse
        )
        throw error
    }.map(response => response.getAwsResponse)
  }

  private def processRequest(client: CloseableHttpAsyncClient, uriRequest: HttpUriRequest): Future[ApacheHttpResponse] = {
    val promise = Promise[ApacheHttpResponse]()
    val callback: FutureCallback[ApacheHttpResponse] = new FutureCallback[ApacheHttpResponse] {
      def cancelled(): Unit = promise.failure(new IllegalStateException("Cancelled request"))
      def completed(t: ApacheHttpResponse): Unit = promise.success(t)
      def failed(e: Exception): Unit = promise.failure(e)
    }
    client.execute(uriRequest, callback)
    promise.future
  }

  private def createResponse(method: HttpRequestBase, request: Request[_], apacheHttpResponse: ApacheHttpResponse): HttpResponse = {
    val httpResponse: HttpResponse = new HttpResponse(request, method)
    if (apacheHttpResponse.getEntity != null) httpResponse.setContent(apacheHttpResponse.getEntity.getContent)
    httpResponse.setStatusCode(apacheHttpResponse.getStatusLine.getStatusCode)
    httpResponse.setStatusText(apacheHttpResponse.getStatusLine.getReasonPhrase)
    for (header <- apacheHttpResponse.getAllHeaders) {
      httpResponse.addHeader(header.getName, header.getValue)
    }
    httpResponse
  }

  private def isRequestSuccessful(response: ApacheHttpResponse): Boolean = {
    val status = getStatusCode(response)
    status / 100 == HttpStatus.SC_OK / 100
  }

  private def isTemporaryRedirect(response: ApacheHttpResponse): Boolean = {
    val status: Int = response.getStatusLine.getStatusCode
    status == HttpStatus.SC_TEMPORARY_REDIRECT && response.getHeaders("Location") != null && response.getHeaders("Location").length > 0
  }

  private def handleResponse[T](request: Request[_],
                                responseHandler: HttpResponseHandler[AmazonWebServiceResponse[T]],
                                method: HttpRequestBase,
                                httpResponse: HttpResponse,
                                apacheHttpResponse: ApacheHttpResponse): T = {
    Try {
      val awsRequest = request.getOriginalRequest
      val inputStream = httpResponse.getContent
      if (inputStream != null) {
        httpResponse.setContent(ProgressInputStream.inputStreamForResponse(inputStream, awsRequest))
      }
      responseHandler.handle(httpResponse)
    } match {
      case Success(response) =>
        response.getResult
      case Failure(throwable) =>
        throw new AmazonClientException("Unable to unmarshall response metadata. Response Code: " + httpResponse.getStatusCode + ", Response Text: " + httpResponse.getStatusText, throwable)
    }
  }

  private def getStatusCode(apacheHttpResponse: ApacheHttpResponse) =
    Option(apacheHttpResponse.getStatusLine).map(_.getStatusCode).getOrElse(-1)

  private def getReasonPhrase(apacheHttpResponse: ApacheHttpResponse) =
    Option(apacheHttpResponse.getStatusLine).map(_.getReasonPhrase).orNull

  private def handleErrorResponse(request: Request[_],
                                  errorResponseHandler: HttpResponseHandler[AmazonServiceException],
                                  method: HttpRequestBase,
                                  apacheHttpResponse: ApacheHttpResponse): AmazonServiceException = {
    val statusCode = getStatusCode(apacheHttpResponse)
    val reasonPhrase = getReasonPhrase(apacheHttpResponse)
    try {
      val response = createResponse(method, request, apacheHttpResponse)
      val exception = errorResponseHandler.handle(response)
      exception.setServiceName(request.getServiceName)
      exception.setStatusCode(statusCode)
      exception.fillInStackTrace
      exception
    } catch {
      case e: Exception if statusCode == 413 =>
        val exception = new AmazonServiceException("Request entity too large")
        exception.setServiceName(request.getServiceName)
        exception.setStatusCode(statusCode)
        exception.setErrorType(ErrorType.Client)
        exception.setErrorCode("Request entity too large")
        exception.fillInStackTrace
        exception
      case e: Exception if statusCode == 503 && "Service Unavailable".equalsIgnoreCase(reasonPhrase) =>
        val exception = new AmazonServiceException("Service unavailable")
        exception.setServiceName(request.getServiceName)
        exception.setStatusCode(statusCode)
        exception.setErrorType(ErrorType.Service)
        exception.setErrorCode("Service unavailable")
        exception.fillInStackTrace
        exception
      case ioe: IOException =>
        throw ioe
      case e: Exception =>
        val errorMessage = s"Unable to unmarshall error response (${e.getMessage}). Response Code: $statusCode, Response Text: $reasonPhrase"
        throw new AmazonClientException(errorMessage, e)
    }
  }

}
