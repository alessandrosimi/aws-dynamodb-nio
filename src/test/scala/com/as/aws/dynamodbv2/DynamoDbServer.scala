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
package com.as.aws.dynamodbv2

import java.io.File
import java.net.URI
import java.util.UUID
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}

import com.almworks.sqlite4java.SQLite
import com.amazonaws.services.dynamodbv2.dataMembers.ResponseData
import com.amazonaws.services.dynamodbv2.exceptions.AmazonServiceExceptionType
import com.amazonaws.services.dynamodbv2.local.exceptions.ExceptionBean
import com.amazonaws.services.dynamodbv2.local.server.{DynamoDBProxyServer, DynamoDBRequestHandler, LocalDynamoDBRequestHandler, LocalDynamoDBServerHandler}
import org.eclipse.jetty.server.Request

import scala.util.Try

class DynamoDbServer(port: Int = 8989) {

  private val RunInMemory = true
  private val EmptyDbPath: String = null
  private val NonSharedDb = false
  private val NonDelayedTransientStatuses = false
  private val EmptyCorsParams: String  = null
  private val requestHandler = new LocalDynamoDBRequestHandler(0, RunInMemory, EmptyDbPath, NonSharedDb, NonDelayedTransientStatuses)
  private val serverHandler = new LocalDynamoDBServerHandlerWithException(requestHandler)
  private lazy val server = new DynamoDBProxyServer(port, serverHandler)

  def start() = {
    loadSqlLiteLibraries()
    server.start()
  }

  private def loadSqlLiteLibraries() {
    val sqlLiteJar = new File(classOf[SQLite].getProtectionDomain.getCodeSource.getLocation.toURI)
    val version = sqlLiteJar.getParentFile
    val artifact = version.getParentFile
    val group = artifact.getParentFile
    group.listFiles().toList
      .filter(file => file.getName != artifact.getName)
      .flatMap(_.listFiles().toList.filter(file => file.getName == version.getName))
      .map(_.getPath)
      .foreach(path => Try {
        SQLite.setLibraryPath(path)
        SQLite.loadLibrary()
      })
  }

  def stop() = server.stop()

  def endpoint = URI.create(s"http://localhost:$port")

  Runtime.getRuntime.addShutdownHook(new Thread() {
    override def run():Unit = DynamoDbServer.this.stop()
  })

  private var exceptionType: HandlerBehaviour = Successful

  def forceFailureWith(exception: AmazonServiceExceptionType) = this.exceptionType = InjectedFailure(
    responseCode = exception.getResponseStatus,
    errorCode = exception.getErrorCode,
    errorMessage = exception.getMessage
  )

  def forceFailureWith(responseCode: Int, errorCode: String = "", errorMessage: String = "") = this.exceptionType = InjectedFailure(
    responseCode = responseCode,
    errorCode = errorCode,
    errorMessage = errorMessage
  )

  def clearForcedFailure() = this.exceptionType = Successful

  private sealed trait HandlerBehaviour
  private object Successful extends HandlerBehaviour
  private object CancelRequest extends HandlerBehaviour
  private case class InjectedFailure(responseCode: Int, errorCode: String, errorMessage: String) extends HandlerBehaviour

  private class LocalDynamoDBServerHandlerWithException(handler: DynamoDBRequestHandler)
    extends LocalDynamoDBServerHandler(handler, EmptyCorsParams) {

    override def handle(target: String,
                        baseRequest: Request,
                        request: HttpServletRequest,
                        response: HttpServletResponse): Unit = exceptionType match {
      case Successful =>
        super.handle(target, baseRequest, request, response)
      case InjectedFailure(responseCode, errorCode, errorMessage) =>
        baseRequest.setHandled(true)
        val res: ResponseData = new ResponseData(response)
        res.getHttpServletResponse.setStatus(responseCode)
        res.setResponseBody(this.jsonMapper.writeValueAsBytes(new ExceptionBean(errorCode, errorMessage)))
        response.setHeader("x-amzn-RequestId", UUID.randomUUID.toString)
        response.getOutputStream.write(res.getResponseBody)
    }

  }

}
