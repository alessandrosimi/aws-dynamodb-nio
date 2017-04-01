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

import com.amazonaws.services.dynamodbv2.model._

import scala.collection.JavaConverters._

trait DynamoDBOperations {
  test: AbstractTest =>

  def createTable(name: String,
                  hashKeyName: String = "key",
                  readThroughput: Int = 10,
                  writeThroughput: Int =  10): CreateTableResult = {
    val attributeDefinition = new AttributeDefinition()
      .withAttributeName(hashKeyName)
      .withAttributeType(ScalarAttributeType.S)
    val keySchema = new KeySchemaElement()
      .withAttributeName(hashKeyName)
      .withKeyType(KeyType.HASH)
    val provisionedThroughput = new ProvisionedThroughput()
      .withReadCapacityUnits(readThroughput.toLong)
      .withWriteCapacityUnits(writeThroughput.toLong)
    resultOf(client.createTable(
      attributeDefinitions = List(attributeDefinition),
      tableName = name,
      keySchema = List(keySchema),
      provisionedThroughput = provisionedThroughput
    ))
  }

  def describeTable(name: String) = resultOf(client.describeTable("tableName")).getTable

  def putItem(tableName: String, values: Map[String, String]): PutItemResult = {
    resultOf(client.putItem(tableName, values.mapValues(new AttributeValue(_))))
  }

  def putWriteRequest(values: Map[String, String]): WriteRequest = {
    val writeRequest = new WriteRequest()
    val putRequest = new PutRequest(values.mapValues(new AttributeValue(_)).asJava)
    writeRequest.setPutRequest(putRequest)
    writeRequest
  }

  def keyAndAttributes(key: String, value: String): KeysAndAttributes = {
    val keysAndAttributes = new KeysAndAttributes()
    val keys = Map("key" -> new AttributeValue(value)).asJava
    keysAndAttributes.setKeys(List(keys).asJava)
    keysAndAttributes
  }

}
