package com.as.aws.dynamodbv2

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
