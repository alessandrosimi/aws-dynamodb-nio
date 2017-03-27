package com.as.aws.dynamodbv2

import java.io.File
import java.net.URI

import com.almworks.sqlite4java.SQLite
import com.amazonaws.services.dynamodbv2.local.server.{DynamoDBProxyServer, LocalDynamoDBRequestHandler, LocalDynamoDBServerHandler}

class LocalDynamoDb {

  val port = 8989
  private val RunInMemory = true
  private val EmptyDbPath: String = null
  private val NonSharedDb = false
  private val NonDelayedTransientStatuses = false
  private val EmptyCorsParams: String  = null
  private val requestHandler = new LocalDynamoDBRequestHandler(0, RunInMemory, EmptyDbPath, NonSharedDb, NonDelayedTransientStatuses)
  private val serverHandler = new LocalDynamoDBServerHandler(requestHandler, EmptyCorsParams)
  private lazy val server = new DynamoDBProxyServer(port, serverHandler)

  def start() = {
    addStorageLibraryToClassPath()
    server.start()
  }

  private def addStorageLibraryToClassPath() = {
    val library = "libsqlite4java-linux-amd64.so"
    val libraryUri = getClass.getClassLoader.getResource(library).toURI
    val libraryPath = new File(libraryUri).getParent
    SQLite.setLibraryPath(libraryPath)
  }

  def stop() = server.stop()

  def endpoint = URI.create(s"http://localhost:$port")

  Runtime.getRuntime.addShutdownHook(new Thread() {
    override def run():Unit = LocalDynamoDb.this.stop()
  })

}
