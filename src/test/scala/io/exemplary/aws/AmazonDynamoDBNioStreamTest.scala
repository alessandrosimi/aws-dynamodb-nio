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

import com.amazonaws.ClientConfiguration
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.http.{Invoker, JsonErrorResponseHandlerV2}
import com.amazonaws.internal.StaticCredentialsProvider
import com.amazonaws.services.dynamodbv2.model._
import com.amazonaws.services.dynamodbv2.model.transform._
import com.amazonaws.transform.JsonErrorUnmarshallerV2

import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.Future

class AmazonDynamoDBNioStreamTest extends AbstractTest with DynamoDBOperations {

  feature("Stream") {

    scenario("creation") {
      try {
        createTable(name = "tableName", hashKeyName = "key",streamEnabled = true)
        val tableDescription = resultOf(client.describeTable("tableName"))
        val streamArn = tableDescription.getTable.getLatestStreamArn
        streamArn should not be null
        val streamSpecification = tableDescription.getTable.getStreamSpecification
        streamSpecification.getStreamEnabled shouldBe true
        putItem("tableName", Map("key" -> "1", "field" -> "value"))
        invoker.start()
        val describeRequest = describeStream(new DescribeStreamRequest().withStreamArn(streamArn))
        val streamDescription = resultOf(describeRequest)
        val shards = streamDescription.getStreamDescription.getShards.asScala
        shards should have size 1
        val shardId = shards.head.getShardId
        val iteratorRequest = new GetShardIteratorRequest()
          .withStreamArn(streamArn)
          .withShardId(shardId)
          .withShardIteratorType(ShardIteratorType.TRIM_HORIZON)
        val iterator = resultOf(getShardIterator(iteratorRequest)).getShardIterator
        iterator should not be null
        val getRecordResult = resultOf(getRecords(new GetRecordsRequest().withShardIterator(iterator)))
        val records = getRecordResult.getRecords.asScala
        records should have size 1
        val keys = records.head.getDynamodb.getKeys
        keys.get("key").getS should be ("1")
        val values = records.head.getDynamodb.getNewImage
        values.get("field").getS should be ("value")
      } finally {
        invoker.stop()
      }
    }

  }

  def describeStream(describeStreamRequest: DescribeStreamRequest): Future[DescribeStreamResult] = invoker.invoke(
    req = describeStreamRequest,
    marshaller = new DescribeStreamRequestMarshaller,
    unmarshaller = new DescribeStreamResultJsonUnmarshaller
  )

  def getShardIterator(getShardIteratorRequest: GetShardIteratorRequest): Future[GetShardIteratorResult] = invoker.invoke(
    req = getShardIteratorRequest,
    marshaller = new GetShardIteratorRequestMarshaller,
    unmarshaller = new GetShardIteratorResultJsonUnmarshaller
  )

  def getRecords(getRecordsRequest: GetRecordsRequest): Future[GetRecordsResult] = invoker.invoke(
    req = getRecordsRequest,
    marshaller = new GetRecordsRequestMarshaller,
    unmarshaller = new GetRecordsResultJsonUnmarshaller
  )

  val DefaultEndpoint = "https://streams.dynamodb.us-east-1.amazonaws.com"

  private val errorResponseHandler = new JsonErrorResponseHandlerV2(List(
    new JsonErrorUnmarshallerV2(classOf[InternalServerErrorException], "InternalServerError"),
    new JsonErrorUnmarshallerV2(classOf[TrimmedDataAccessException], "TrimmedDataAccessException"),
    new JsonErrorUnmarshallerV2(classOf[ExpiredIteratorException], "ExpiredIteratorException"),
    JsonErrorUnmarshallerV2.DEFAULT_UNMARSHALLER
  ).asJava)

  val invoker = new Invoker(
    serviceName = "dynamodb",
    endpoint = server.endpoint,
    awsCredentialsProvider = new StaticCredentialsProvider(new BasicAWSCredentials("accessKey", "secretKey")),
    config = new ClientConfiguration(),
    errorResponseHandler = errorResponseHandler,
    executionContext = global
  )

}
