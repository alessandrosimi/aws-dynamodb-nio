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
package io.exemplary.aws

import java.net.URI

import com.amazonaws.ClientConfiguration
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.internal.StaticCredentialsProvider

import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext.Implicits._

class AmazonDynamoDBNioGzipTest extends AbstractTest with DynamoDBOperations {

  feature("Table") {

    scenario("creation") {
      val client = createClient
      var tables = resultOf(client.listTables)
      tables.getTableNames should be (empty)
      val creationResult = createTable(name = "tableName")
      val tableDescription = creationResult.getTableDescription
      tableDescription should not be null
      tables = resultOf(client.listTables)
      tables.getTableNames should have size 1
      tables.getTableNames.asScala.head should be ("tableName")
      client.shutdown()
    }

  }

  val clientConfigWithGzip = {
    val config = new ClientConfiguration()
    config.setUseGzip(true)
    config
  }

  def createClient = new AmazonDynamoDBNioClient(
    endpoint = URI.create(server.getEndpoint),
    awsCredentialsProvider = new StaticCredentialsProvider(new BasicAWSCredentials("accessKey", "secretKey")),
    config = clientConfigWithGzip
  )(global)

}
