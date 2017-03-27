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

import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.internal.StaticCredentialsProvider
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, FeatureSpec, Matchers}

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext.Implicits.global

abstract class AbstractTest extends FeatureSpec with BeforeAndAfterAll with BeforeAndAfter with Matchers {

  def resultOf[A](futureResult: Future[A]): A = Await.result(futureResult, 1.minute)

  private val localDynamoDb = new LocalDynamoDb

  val client = new AmazonDynamoDBNioClient(
    endpoint = localDynamoDb.endpoint,
    awsCredentialsProvider = new StaticCredentialsProvider(new BasicAWSCredentials("accessKey", "secretKey"))
  )(global)

  override def beforeAll(): Unit = {
    localDynamoDb.start()
  }

  before {
    resultOf(client.listTables)
      .getTableNames.asScala
      .foreach( tableName => resultOf(client.deleteTable(tableName)) )
  }

  override def afterAll(): Unit = {
    client.shutdown()
    localDynamoDb.stop()
  }

}
