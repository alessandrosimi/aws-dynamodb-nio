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
