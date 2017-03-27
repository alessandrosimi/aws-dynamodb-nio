package com.as.aws.dynamodbv2

import java.net.URI

import com.amazonaws.ClientConfiguration
import com.amazonaws.auth.{AWSCredentialsProvider, DefaultAWSCredentialsProviderChain}
import com.amazonaws.http.{Invoker, JsonErrorResponseHandlerV2}
import com.amazonaws.internal.DefaultServiceEndpointBuilder
import com.amazonaws.regions.{Region, Regions}
import com.as.aws.dynamodbv2.AmazonDynamoDBNioClient._
import com.amazonaws.services.dynamodbv2.model._
import com.amazonaws.services.dynamodbv2.model.transform._
import com.amazonaws.transform.JsonErrorUnmarshallerV2

import scala.collection.JavaConverters._
import scala.concurrent.{ExecutionContext, Future}

class AmazonDynamoDBNioClient(private[dynamodbv2] val awsCredentialsProvider: AWSCredentialsProvider = new DefaultAWSCredentialsProviderChain,
                              private[dynamodbv2] val config: ClientConfiguration = new ClientConfiguration(),
                              private[dynamodbv2] val endpoint: URI = DefaultEndpoint)(implicit executionContext: ExecutionContext)
  extends AmazonDynamoDBNio[AmazonDynamoDBNioClient] {

  def this(region: Regions)(implicit executionContext: ExecutionContext) = this(
    awsCredentialsProvider = new DefaultAWSCredentialsProviderChain,
    config = new ClientConfiguration(),
    endpoint = convertEndpointFromRegion(Region.getRegion(region), new ClientConfiguration())
  )

  def this(region: Regions,
           awsCredentialsProvider: AWSCredentialsProvider)(implicit executionContext: ExecutionContext) = this(
    awsCredentialsProvider = awsCredentialsProvider,
    config = new ClientConfiguration(),
    endpoint = convertEndpointFromRegion(Region.getRegion(region), new ClientConfiguration())
  )

  def this(region: Regions,
           awsCredentialsProvider: AWSCredentialsProvider,
           config: ClientConfiguration)(implicit executionContext: ExecutionContext) = this(
    awsCredentialsProvider = awsCredentialsProvider,
    config = config,
    endpoint = convertEndpointFromRegion(Region.getRegion(region), config)
  )

  private val errorResponseHandler = new JsonErrorResponseHandlerV2(List(
    new JsonErrorUnmarshallerV2(classOf[ResourceInUseException], "ResourceInUseException"),
    new JsonErrorUnmarshallerV2(classOf[ItemCollectionSizeLimitExceededException], "ItemCollectionSizeLimitExceededException"),
    new JsonErrorUnmarshallerV2(classOf[LimitExceededException], "LimitExceededException"),
    new JsonErrorUnmarshallerV2(classOf[ConditionalCheckFailedException], "ConditionalCheckFailedException"),
    new JsonErrorUnmarshallerV2(classOf[ProvisionedThroughputExceededException], "ProvisionedThroughputExceededException"),
    new JsonErrorUnmarshallerV2(classOf[InternalServerErrorException], "InternalServerError"),
    new JsonErrorUnmarshallerV2(classOf[ResourceNotFoundException], "ResourceNotFoundException"),
    JsonErrorUnmarshallerV2.DEFAULT_UNMARSHALLER
  ).asJava)

  private val internalInvoker = new Invoker(
    serviceName = ServiceName,
    endpoint = endpoint,
    awsCredentialsProvider = awsCredentialsProvider,
    config = config,
    errorResponseHandler = errorResponseHandler,
    executionContext = executionContext
  )

  private lazy val invoker = internalInvoker.start() // It would start the first time is used

  override def setEndpoint(endpoint: String): AmazonDynamoDBNioClient = {
    shutdown()
    new AmazonDynamoDBNioClient(awsCredentialsProvider, config, URI.create(endpoint))
  }

  override def setRegion(region: Region): AmazonDynamoDBNioClient = {
    shutdown()
    new AmazonDynamoDBNioClient(Regions.fromName(region.getName), awsCredentialsProvider, config)
  }

  override def batchGetItem(batchGetItemRequest: BatchGetItemRequest): Future[BatchGetItemResult] = invoker.invoke(
    req = batchGetItemRequest,
    marshaller = new BatchGetItemRequestMarshaller,
    unmarshaller = new BatchGetItemResultJsonUnmarshaller
  )

  override def batchGetItem(requestItems: Map[String, KeysAndAttributes],
                            returnConsumedCapacity: String): Future[BatchGetItemResult] = batchGetItem(
    new BatchGetItemRequest()
      .withRequestItems(requestItems.asJava)
      .withReturnConsumedCapacity(returnConsumedCapacity)
  )

  override def batchGetItem(requestItems: Map[String, KeysAndAttributes]): Future[BatchGetItemResult] = batchGetItem(
    new BatchGetItemRequest()
      .withRequestItems(requestItems.asJava)
  )

  override def batchWriteItem(batchWriteItemRequest: BatchWriteItemRequest): Future[BatchWriteItemResult] = invoker.invoke(
    req = batchWriteItemRequest,
    marshaller = new BatchWriteItemRequestMarshaller,
    unmarshaller = new BatchWriteItemResultJsonUnmarshaller
  )

  override def batchWriteItem(requestItems: Map[String, List[WriteRequest]]): Future[BatchWriteItemResult] = batchWriteItem(
    new BatchWriteItemRequest()
      .withRequestItems(requestItems.mapValues(_.asJava).asJava)
  )

  override def createTable(createTableRequest: CreateTableRequest): Future[CreateTableResult] = invoker.invoke(
    req = createTableRequest,
    marshaller = new CreateTableRequestMarshaller,
    unmarshaller = new CreateTableResultJsonUnmarshaller
  )

  override def createTable(attributeDefinitions: List[AttributeDefinition],
                           tableName: String,
                           keySchema: List[KeySchemaElement],
                           provisionedThroughput: ProvisionedThroughput): Future[CreateTableResult] = createTable(
    new CreateTableRequest()
      .withAttributeDefinitions(attributeDefinitions.asJava)
      .withTableName(tableName)
      .withKeySchema(keySchema.asJava)
      .withProvisionedThroughput(provisionedThroughput)
  )

  override def deleteItem(deleteItemRequest: DeleteItemRequest): Future[DeleteItemResult] = invoker.invoke(
    req = deleteItemRequest,
    marshaller = new DeleteItemRequestMarshaller,
    unmarshaller = new DeleteItemResultJsonUnmarshaller
  )

  override def deleteItem(tableName: String, key: Map[String, AttributeValue]): Future[DeleteItemResult] = deleteItem(
    new DeleteItemRequest()
      .withTableName(tableName)
      .withKey(key.asJava)
  )

  override def deleteItem(tableName: String,
                          key: Map[String, AttributeValue],
                          returnValues: String): Future[DeleteItemResult] = deleteItem(
    new DeleteItemRequest()
      .withTableName(tableName)
      .withKey(key.asJava)
      .withReturnValues(returnValues)
  )

  override def deleteTable(deleteTableRequest: DeleteTableRequest): Future[DeleteTableResult] = invoker.invoke(
    req = deleteTableRequest,
    marshaller = new DeleteTableRequestMarshaller,
    unmarshaller = new DeleteTableResultJsonUnmarshaller
  )

  override def deleteTable(tableName: String): Future[DeleteTableResult] = deleteTable(
    new DeleteTableRequest().withTableName(tableName)
  )

  override def describeTable(describeTableRequest: DescribeTableRequest): Future[DescribeTableResult] = invoker.invoke(
    req = describeTableRequest,
    marshaller = new DescribeTableRequestMarshaller,
    unmarshaller = new DescribeTableResultJsonUnmarshaller
  )

  override def describeTable(tableName: String): Future[DescribeTableResult] = describeTable(
    new DescribeTableRequest().withTableName(tableName)
  )

  override def getItem(getItemRequest: GetItemRequest): Future[GetItemResult] = invoker.invoke(
    req = getItemRequest,
    marshaller = new GetItemRequestMarshaller,
    unmarshaller = new GetItemResultJsonUnmarshaller
  )

  override def getItem(tableName: String, key: Map[String, AttributeValue]): Future[GetItemResult] = getItem(
    new GetItemRequest().withTableName(tableName).withKey(key.asJava)
  )

  override def getItem(tableName: String,
                       key: Map[String, AttributeValue],
                       consistentRead: Boolean): Future[GetItemResult] = getItem(
    new GetItemRequest()
      .withTableName(tableName)
      .withKey(key.asJava)
      .withConsistentRead(consistentRead)
  )

  override def listTables(listTablesRequest: ListTablesRequest): Future[ListTablesResult] = invoker.invoke(
    req = listTablesRequest,
    marshaller = new ListTablesRequestMarshaller,
    unmarshaller = new ListTablesResultJsonUnmarshaller
  )

  override def listTables: Future[ListTablesResult] = listTables(new ListTablesRequest)

  override def listTables(exclusiveStartTableName: String): Future[ListTablesResult] = listTables(
    new ListTablesRequest().withExclusiveStartTableName(exclusiveStartTableName)
  )

  override def listTables(exclusiveStartTableName: String, limit: Integer): Future[ListTablesResult] = listTables(
    new ListTablesRequest()
      .withExclusiveStartTableName(exclusiveStartTableName)
      .withLimit(limit)
  )

  override def listTables(limit: Integer): Future[ListTablesResult] = listTables(
    new ListTablesRequest().withLimit(limit)
  )

  override def putItem(putItemRequest: PutItemRequest): Future[PutItemResult] = invoker.invoke(
    req = putItemRequest,
    marshaller = new PutItemRequestMarshaller,
    unmarshaller = new PutItemResultJsonUnmarshaller
  )

  override def putItem(tableName: String, item: Map[String, AttributeValue]): Future[PutItemResult] =  putItem(
    new PutItemRequest().withTableName(tableName).withItem(item.asJava)
  )

  override def putItem(tableName: String,
                       item: Map[String, AttributeValue],
                       returnValues: String): Future[PutItemResult] = putItem(
    new PutItemRequest()
      .withTableName(tableName)
      .withItem(item.asJava)
      .withReturnValues(returnValues)
  )

  override def query(queryRequest: QueryRequest): Future[QueryResult] = invoker.invoke(
    req = queryRequest,
    marshaller = new QueryRequestMarshaller,
    unmarshaller = new QueryResultJsonUnmarshaller
  )

  override def scan(scanRequest: ScanRequest): Future[ScanResult] = invoker.invoke(
    req = scanRequest,
    marshaller = new ScanRequestMarshaller,
    unmarshaller = new ScanResultJsonUnmarshaller
  )

  override def scan(tableName: String, attributesToGet: List[String]): Future[ScanResult] = scan(
    new ScanRequest()
      .withTableName(tableName)
      .withAttributesToGet(attributesToGet.asJava)
  )

  override def scan(tableName: String, scanFilter: Map[String, Condition]): Future[ScanResult] =  scan(
    new ScanRequest()
      .withTableName(tableName)
      .withScanFilter(scanFilter.asJava)
  )

  override def scan(tableName: String,
                    attributesToGet: List[String],
                    scanFilter: Map[String, Condition]): Future[ScanResult] = scan(
    new ScanRequest()
      .withTableName(tableName)
      .withAttributesToGet(attributesToGet.asJava)
      .withScanFilter(scanFilter.asJava)
  )

  override def updateItem(updateItemRequest: UpdateItemRequest): Future[UpdateItemResult] = invoker.invoke(
    req = updateItemRequest,
    marshaller = new UpdateItemRequestMarshaller,
    unmarshaller = new UpdateItemResultJsonUnmarshaller
  )

  override def updateItem(tableName: String,
                          key: Map[String, AttributeValue],
                          attributeUpdates: Map[String, AttributeValueUpdate]): Future[UpdateItemResult] =  updateItem(
    new UpdateItemRequest()
      .withTableName(tableName)
      .withKey(key.asJava)
      .withAttributeUpdates(attributeUpdates.asJava)
  )

  override def updateItem(tableName: String,
                          key: Map[String, AttributeValue],
                          attributeUpdates: Map[String, AttributeValueUpdate],
                          returnValues: String): Future[UpdateItemResult] = updateItem(
    new UpdateItemRequest()
      .withTableName(tableName)
      .withKey(key.asJava)
      .withAttributeUpdates(attributeUpdates.asJava)
      .withReturnValues(returnValues)
  )

  override def updateTable(updateTableRequest: UpdateTableRequest): Future[UpdateTableResult] = invoker.invoke(
    req = updateTableRequest,
    marshaller = new UpdateTableRequestMarshaller,
    unmarshaller = new UpdateTableResultJsonUnmarshaller
  )

  override def updateTable(tableName: String,
                           provisionedThroughput: ProvisionedThroughput): Future[UpdateTableResult] = updateTable(
    new UpdateTableRequest()
      .withTableName(tableName)
      .withProvisionedThroughput(provisionedThroughput)
  )

  override def shutdown(): Unit = {
    internalInvoker.stop()
  }

}


object AmazonDynamoDBNioClient {

  private val ServiceName = "dynamodb"
  private[dynamodbv2] val DefaultEndpoint = URI.create("https://dynamodb.us-east-1.amazonaws.com")

  private def convertEndpointFromRegion(region: Region, clientConfiguration: ClientConfiguration): URI = {
    new DefaultServiceEndpointBuilder(ServiceName, clientConfiguration.getProtocol.toString).withRegion(region).getServiceEndpoint
  }

}