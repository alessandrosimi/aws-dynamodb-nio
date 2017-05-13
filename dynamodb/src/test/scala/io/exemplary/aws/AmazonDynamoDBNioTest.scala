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

import com.amazonaws.AmazonServiceException
import com.amazonaws.services.dynamodbv2.model._

import scala.collection.JavaConverters._

class AmazonDynamoDBNioTest extends AbstractTest with DynamoDBOperations {

  feature("Table") {

    scenario("creation") {
      var tables = resultOf(client.listTables)
      tables.getTableNames should be (empty)
      val creationResult = createTable(name = "tableName")
      val tableDescription = creationResult.getTableDescription
      tableDescription should not be null
      tables = resultOf(client.listTables)
      tables.getTableNames should have size 1
      tables.getTableNames.asScala.head should be ("tableName")
    }

    scenario("deletion") {
      resultOf(client.listTables).getTableNames should be (empty)
      createTable(name = "tableName")
      resultOf(client.listTables).getTableNames should have size 1
      val deletionResult = resultOf(client.deleteTable("tableName"))
      deletionResult.getTableDescription.getTableName should be ("tableName")
      resultOf(client.listTables).getTableNames should be (empty)
    }

    scenario("list") {
      resultOf(client.listTables).getTableNames should be (empty)
      createTable(name = "oneTableName")
      createTable(name = "twoTableName")
      createTable(name = "twoAfterTableName")
      resultOf(client.listTables).getTableNames should have size 3
      resultOf(client.listTables("two")).getTableNames should have size 2
      resultOf(client.listTables("two", 2)).getTableNames should have size 2
      resultOf(client.listTables("two", 1)).getTableNames should have size 1
      resultOf(client.listTables(1)).getTableNames should have size 1
    }

    scenario("read throughput") {
      resultOf(client.listTables).getTableNames should be (empty)
      createTable(name = "tableName", readThroughput = 20)
      describeTable("tableName").getProvisionedThroughput.getReadCapacityUnits should be (20)
      val updateResult = resultOf(client.updateTable("tableName", new ProvisionedThroughput().withReadCapacityUnits(25L).withWriteCapacityUnits(10L)))
      updateResult.getTableDescription.getTableName should be ("tableName")
      describeTable("tableName").getProvisionedThroughput.getReadCapacityUnits should be (25)
    }

    scenario("write throughput") {
      resultOf(client.listTables).getTableNames should be (empty)
      createTable(name = "tableName", writeThroughput = 20)
      describeTable("tableName").getProvisionedThroughput.getWriteCapacityUnits should be (20)
      val updateResult = resultOf(client.updateTable("tableName", new ProvisionedThroughput().withWriteCapacityUnits(25L).withReadCapacityUnits(10L)))
      updateResult.getTableDescription.getTableName should be ("tableName")
      describeTable("tableName").getProvisionedThroughput.getWriteCapacityUnits should be (25)
    }

  }

  feature("Item") {

    scenario("put") {
      createTable("tableName", "key")
      val key = Map("key" -> new AttributeValue("key"))
      resultOf(client.getItem("tableName", key)).getItem should be (null)
      val value = Map("field" -> new AttributeValue("value"))
      resultOf(client.putItem("tableName", key ++ value))
      val getResult = resultOf(client.getItem("tableName", key, consistentRead = true))
      getResult should not be null
      getResult.getItem should have size 2
      val field = getResult.getItem.asScala.filterKeys(_ == "field")
      field should not be empty
      field("field").getS should be ("value")
    }

    scenario("put with return values") {
      createTable("tableName", "key")
      val key = Map("key" -> new AttributeValue("key"))
      resultOf(client.getItem("tableName", key)).getItem should be (null)
      val value = Map("field" -> new AttributeValue("value"))
      val oldValues = resultOf(client.putItem("tableName", key ++ value, ReturnValue.ALL_OLD.name()))
      oldValues.getAttributes should be (null)
    }

    scenario("update") {
      createTable("tableName", "key")
      val key = Map("key" -> new AttributeValue("key"))
      resultOf(client.getItem("tableName", key)).getItem should be (null)
      val value = Map("field" -> new AttributeValue("value"))
      resultOf(client.putItem("tableName", key ++ value))
      var result = resultOf(client.getItem("tableName", key)).getItem.asScala("field")
      result.getS should be ("value")
      val updatedValue = Map("field" -> {
        val update = new AttributeValueUpdate()
        update.setValue(new AttributeValue("updatedValue"))
        update.setAction(AttributeAction.PUT)
        update
      })
      resultOf(client.updateItem("tableName", key, updatedValue))
      result = resultOf(client.getItem("tableName", key)).getItem.asScala("field")
      result.getS should be ("updatedValue")
    }

    scenario("update with return values") {
      createTable("tableName", "key")
      val key = Map("key" -> new AttributeValue("key"))
      resultOf(client.getItem("tableName", key)).getItem should be (null)
      val value = Map("field" -> new AttributeValue("value"))
      resultOf(client.putItem("tableName", key ++ value))
      var result = resultOf(client.getItem("tableName", key)).getItem.asScala("field")
      result.getS should be ("value")
      val updatedValue = Map("field" -> {
        val update = new AttributeValueUpdate()
        update.setValue(new AttributeValue("updatedValue"))
        update.setAction(AttributeAction.PUT)
        update
      })
      val oldValue = resultOf(client.updateItem("tableName", key, updatedValue, ReturnValue.UPDATED_OLD.name()))
      oldValue.getAttributes.asScala("field").getS should be ("value")
    }

    scenario("delete") {
      createTable("tableName", "key")
      val key = Map("key" -> new AttributeValue("key"))
      resultOf(client.getItem("tableName", key)).getItem should be (null)
      val value = Map("field" -> new AttributeValue("value"))
      resultOf(client.putItem("tableName", key ++ value))
      resultOf(client.getItem("tableName", key)).getItem should not be null
      resultOf(client.deleteItem("tableName", key))
      resultOf(client.getItem("tableName", key)).getItem should be (null)
    }

    scenario("delete with return values") {
      createTable("tableName", "key")
      val key = Map("key" -> new AttributeValue("key"))
      resultOf(client.getItem("tableName", key)).getItem should be (null)
      val value = Map("field" -> new AttributeValue("value"))
      resultOf(client.putItem("tableName", key ++ value))
      resultOf(client.getItem("tableName", key)).getItem should not be null
      val oldValues = resultOf(client.deleteItem("tableName", key, ReturnValue.ALL_OLD.name))
      oldValues.getAttributes.asScala("key").getS should be ("key")
      resultOf(client.getItem("tableName", key)).getItem should be (null)
    }

    scenario("scan") {
      createTable("tableName", "key")
      putItem("tableName", Map("key" -> "key1", "value" -> "value1", "otherValue" -> "otherValue1"))
      putItem("tableName", Map("key" -> "key2", "value" -> "value2", "otherValue" -> "otherValue2"))
      val scanResult = resultOf(client.scan("tableName", List("otherValue")))
      scanResult.getCount should be (2)
      val withOtherValue = scanResult.getItems.asScala.filter( item => item.containsKey("otherValue") )
      withOtherValue should have size 2
      val withoutOtherValue = scanResult.getItems.asScala.filter( item => item.containsKey("value") )
      withoutOtherValue should have size 0
    }

    scenario("scan with filter") {
      createTable("tableName", "key")
      putItem("tableName", Map("key" -> "key1", "value" -> "value1", "otherValue" -> "otherValue1"))
      putItem("tableName", Map("key" -> "key2", "value" -> "value2", "otherValue" -> "otherValue2"))
      putItem("tableName", Map("key" -> "key3", "value" -> "value3", "otherValue" -> "otherValue3"))
      val condition = {
        val condition = new Condition()
        condition.setComparisonOperator(ComparisonOperator.CONTAINS)
        condition.setAttributeValueList(List(new AttributeValue("2")).asJava)
        condition
      }
      var scanResult = resultOf(client.scan("tableName", List("otherValue"), Map("value" -> condition)))
      scanResult.getCount should be (1)
      scanResult.getItems.get(0).asScala("otherValue").getS should be ("otherValue2")
      scanResult.getItems.get(0).asScala.get("value") should not be defined
      scanResult = resultOf(client.scan("tableName", Map("value" -> condition)))
      scanResult.getCount should be (1)
      scanResult.getItems.get(0).asScala("otherValue").getS should be ("otherValue2")
      scanResult.getItems.get(0).asScala("value").getS should be ("value2")
    }

    scenario("query") {
      createTable("tableName", "key")
      putItem("tableName", Map("key" -> "key1", "value" -> "value1", "otherValue" -> "otherValue1"))
      putItem("tableName", Map("key" -> "key2", "value" -> "value2", "otherValue" -> "otherValue2"))
      putItem("tableName", Map("key" -> "key3", "value" -> "value3", "otherValue" -> "otherValue3"))
      val condition = {
        val condition = new Condition()
        condition.setComparisonOperator(ComparisonOperator.EQ)
        condition.setAttributeValueList(List(new AttributeValue("key1")).asJava)
        condition
      }
      val queryRequest = new QueryRequest("tableName").addKeyConditionsEntry("key", condition)
      val queryResult = resultOf(client.query(queryRequest))
      queryResult.getCount should be (1)
    }

  }

  feature("Batch") {

    scenario("write and read") {
      createTable("tableName", "key")
      val writes = Map(
        "tableName" -> List(
          putWriteRequest(Map("key" -> "key1", "value" -> "value1")),
          putWriteRequest(Map("key" -> "key2", "value" -> "value2"))
        )
      )
      val batchWriteResult = resultOf(client.batchWriteItem(writes))
      batchWriteResult.getUnprocessedItems.asScala should have size 0
      val gets = Map(
        "tableName" -> keyAndAttributes("key", "key1")
      )
      var batchGetResult = resultOf(client.batchGetItem(gets))
      val table = batchGetResult.getResponses.asScala.get("tableName")
      table should be (defined)
      val items = table.get.asScala
      items should have size 1
      items.head.asScala("key").getS should be ("key1")
      batchGetResult = resultOf(client.batchGetItem(gets, "consumedCapacity"))
    }

  }

  feature("Handling failures") {

    scenario("fails to list tables") {
      val error = intercept[AmazonServiceException] {
        resultOf(client.listTables("a"))
      }
      error.getErrorMessage.toLowerCase should include ("invalid table")
    }

  }

}
