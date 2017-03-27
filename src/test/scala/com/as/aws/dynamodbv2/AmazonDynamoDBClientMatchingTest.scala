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

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import org.scalatest.{FeatureSpec, Matchers}

class AmazonDynamoDBClientMatchingTest extends FeatureSpec with Matchers {

  val NotImplementedMethod = "getCachedResponseMetadata"

  val awsClientMethods = classOf[AmazonDynamoDB]
    .getDeclaredMethods
    .map(_.getName)
    .filter(_ != NotImplementedMethod)
    .sorted
  val nioClientMethods = classOf[AmazonDynamoDBNio[_]]
    .getDeclaredMethods
    .map(_.getName)
    .sorted

  scenario("nio client should match aws client methods") {
    awsClientMethods should be(nioClientMethods)
  }

}
