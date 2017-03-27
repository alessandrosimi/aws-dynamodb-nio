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

import java.net.URI

import com.amazonaws.ClientConfiguration
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain
import com.amazonaws.auth.profile.ProfileCredentialsProvider
import com.amazonaws.regions.{Region, Regions}
import org.scalatest.{FeatureSpec, Matchers}

import scala.concurrent.ExecutionContext.Implicits.global

class AmazonDynamoDBNioClientTest extends FeatureSpec with Matchers {

  feature("Constructor") {

    scenario("default") {
      val client = new AmazonDynamoDBNioClient()(global)
      client.endpoint should be (AmazonDynamoDBNioClient.DefaultEndpoint)
      client.awsCredentialsProvider shouldBe a[DefaultAWSCredentialsProviderChain]
      client.config shouldBe a[ClientConfiguration]
    }

    scenario("endpoint") {
      val endpoint = URI.create("http://custom")
      val client = new AmazonDynamoDBNioClient(endpoint = endpoint)
      client.endpoint should be (endpoint)
    }

    scenario("regions") {
      val client = new AmazonDynamoDBNioClient(Regions.EU_WEST_1)
      client.endpoint.getHost should include (Regions.EU_WEST_1.getName)
    }

    scenario("regions and credentials") {
      val client = new AmazonDynamoDBNioClient(Regions.EU_WEST_1, new ProfileCredentialsProvider)
      client.endpoint.getHost should include (Regions.EU_WEST_1.getName)
      client.awsCredentialsProvider shouldBe a[ProfileCredentialsProvider]
    }

  }


  feature("Endpoint and Region") {

    scenario("set endpoint") {
      var client = new AmazonDynamoDBNioClient()
      client.endpoint should be (AmazonDynamoDBNioClient.DefaultEndpoint)
      client = client.setEndpoint("http://custom")
      client.endpoint should be (URI.create("http://custom"))
    }

    scenario("set region") {
      var client = new AmazonDynamoDBNioClient()
      client.endpoint.getHost should not include Regions.EU_WEST_1.getName
      client = client.setRegion(Region.getRegion(Regions.EU_WEST_1))
      client.endpoint.getHost should include (Regions.EU_WEST_1.getName)
    }

  }

}
