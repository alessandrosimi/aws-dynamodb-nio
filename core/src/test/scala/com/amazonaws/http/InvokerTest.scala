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

import com.amazonaws.{AmazonClientException, AmazonServiceException, ClientConfiguration}
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.internal.StaticCredentialsProvider
import com.amazonaws.services.dynamodbv2.exceptions.AmazonServiceExceptionType
import com.amazonaws.services.dynamodbv2.model.{ListTablesRequest, ListTablesResult}
import com.amazonaws.services.dynamodbv2.model.transform.{ListTablesRequestMarshaller, ListTablesResultJsonUnmarshaller}
import com.amazonaws.transform.JsonUnmarshallerContext

import scala.concurrent.ExecutionContext.Implicits.global

class InvokerTest extends AbstractTest {

  scenario("fail to unmarshall successful response") {
    val invoker = createInvoker()
    invoker.start()
    val error = intercept[AmazonClientException] {
      invoke(invoker, failToUnmarshall = true)
    }
    error.getMessage should include ("unmarshall")
    error.getMessage should include ("200")
    invoker.stop()
  }

  feature("Handle failure") {

    scenario("IO Exception") {
      val ioException = new IOException("Failing for IO Exception")
      val invoker = createInvoker(new FailingHandler(ioException))
      invoker.start()
      server.failsWith(AmazonServiceExceptionType.INTERNAL_FAILURE)
      val error = intercept[IOException] {
        invoke(invoker)
      }
      error should be (ioException)
      invoker.stop()
    }

    scenario("Request entity too large") {
      val invoker = createInvoker(new FailingHandler())
      invoker.start()
      server.failsWithResponseCode(413)
      val error = intercept[AmazonServiceException] {
        invoke(invoker)
      }
      error.getStatusCode should be (413)
      error.getErrorMessage should be ("Request entity too large")
      invoker.stop()
    }

    scenario("Service unavailable") {
      val invoker = createInvoker(new FailingHandler())
      invoker.start()
      server.failsWithResponseCode(503)
      val error = intercept[AmazonServiceException] {
        invoke(invoker)
      }
      error.getStatusCode should be (503)
      error.getErrorMessage should be ("Service unavailable")
      invoker.stop()
    }

    scenario("Handler exception") {
      val handlerException = new Exception("Handler exception")
      val invoker = createInvoker(new FailingHandler(handlerException))
      invoker.start()
      server.failsWithResponseCode(999)
      val error = intercept[AmazonClientException] {
        invoke(invoker)
      }
      error.getCause should be (handlerException)
      invoker.stop()
    }

  }

  def invoke(invoker: Invoker, failToUnmarshall: Boolean = false) = resultOf {
    invoker.invoke(
      req = new ListTablesRequest(),
      marshaller = new ListTablesRequestMarshaller,
      unmarshaller = new ListTablesResultJsonUnmarshaller {
        override def unmarshall(context: JsonUnmarshallerContext): ListTablesResult = {
          if (failToUnmarshall) throw new Exception("Injected failure")
          super.unmarshall(context)
        }
      }
    )
  }

  def createInvoker(errorResponseHandler: HttpResponseHandler[AmazonServiceException] = new FailingHandler) = new Invoker(
    serviceName = "dynamodb",
    endpoint = URI.create(server.getEndpoint),
    awsCredentialsProvider = new StaticCredentialsProvider(new BasicAWSCredentials("accessKey", "secretKey")),
    config = new ClientConfiguration(),
    errorResponseHandler = errorResponseHandler,
    executionContext = global
  )

  class FailingHandler(throwable: Throwable = new Exception("Generic failure"))
    extends HttpResponseHandler[AmazonServiceException] {
    def needsConnectionLeftOpen(): Boolean = false
    def handle(response: HttpResponse): AmazonServiceException = throw throwable
  }

}
