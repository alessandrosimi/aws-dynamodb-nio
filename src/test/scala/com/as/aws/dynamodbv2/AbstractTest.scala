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
  )

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
