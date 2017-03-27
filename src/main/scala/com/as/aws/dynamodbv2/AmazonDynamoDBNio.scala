package com.as.aws.dynamodbv2

import com.amazonaws.regions.Region
import com.amazonaws.services.dynamodbv2.model._

import scala.concurrent.Future

trait AmazonDynamoDBNio[T <: AmazonDynamoDBNio[T]] {

  /**
    * Overrides the default endpoint for this client
    * ("https://dynamodb.us-east-1.amazonaws.com"). Callers can use this method
    * to control which AWS region they want to work with.
    * <p>
    * Callers can pass in just the endpoint (ex:
    * "dynamodb.us-east-1.amazonaws.com") or a full URL, including the protocol
    * (ex: "https://dynamodb.us-east-1.amazonaws.com"). If the protocol is not
    * specified here, the default protocol from this client's
    * {@link ClientConfiguration} will be used, which by default is HTTPS.
    * <p>
    * For more information on using AWS regions with the AWS SDK for Java, and
    * a complete list of all available endpoints for all AWS services, see: <a
    * href=
    * "http://developer.amazonwebservices.com/connect/entry.jspa?externalID=3912"
    * > http://developer.amazonwebservices.com/connect/entry.jspa?externalID=
    * 3912</a>
    * <p>
    * <b>This method is not threadsafe. An endpoint should be configured when
    * the client is created and before any service requests are made. Changing
    * it afterwards creates inevitable race conditions for any service requests
    * in transit or retrying.</b>
    *
    * @param endpoint
    * The endpoint (ex: "dynamodb.us-east-1.amazonaws.com") or a full
    * URL, including the protocol (ex:
    * "https://dynamodb.us-east-1.amazonaws.com") of the region specific
    * AWS endpoint this client will communicate with.
    */
  def setEndpoint(endpoint: String): T

  /**
    * An alternative to {@link AmazonDynamoDB#setEndpoint(String)}, sets the
    * regional endpoint for this client's service calls. Callers can use this
    * method to control which AWS region they want to work with.
    * <p>
    * By default, all service endpoints in all regions use the https protocol.
    * To use http instead, specify it in the {@link ClientConfiguration}
    * supplied at construction.
    * <p>
    * <b>This method is not threadsafe. A region should be configured when the
    * client is created and before any service requests are made. Changing it
    * afterwards creates inevitable race conditions for any service requests in
    * transit or retrying.</b>
    *
    * @param region
    * The region this client will communicate with. See
    * { @link Region#getRegion(com.amazonaws.regions.Regions)} for
    *         accessing a given region. Must not be null and must be a region
    *         where the service is available.
    * @see Region#getRegion(com.amazonaws.regions.Regions)
    * @see Region#createClient(Class,
    *      com.amazonaws.auth.AWSCredentialsProvider, ClientConfiguration)
    * @see Region#isServiceSupported(String)
    */
  def setRegion(region: Region): T

  /**
    * <p>
    * The <i>BatchGetItem</i> operation returns the attributes of one or more
    * items from one or more tables. You identify requested items by primary
    * key.
    * </p>
    * <p>
    * A single operation can retrieve up to 16 MB of data, which can contain as
    * many as 100 items. <i>BatchGetItem</i> will return a partial result if
    * the response size limit is exceeded, the table's provisioned throughput
    * is exceeded, or an internal processing failure occurs. If a partial
    * result is returned, the operation returns a value for
    * <i>UnprocessedKeys</i>. You can use this value to retry the operation
    * starting with the next item to get.
    * </p>
    * <important>
    * <p>
    * If you request more than 100 items <i>BatchGetItem</i> will return a
    * <i>ValidationException</i> with the message
    * "Too many items requested for the BatchGetItem call".
    * </p>
    * </important>
    * <p>
    * For example, if you ask to retrieve 100 items, but each individual item
    * is 300 KB in size, the system returns 52 items (so as not to exceed the
    * 16 MB limit). It also returns an appropriate <i>UnprocessedKeys</i> value
    * so you can get the next page of results. If desired, your application can
    * include its own logic to assemble the pages of results into one data set.
    * </p>
    * <p>
    * If <i>none</i> of the items can be processed due to insufficient
    * provisioned throughput on all of the tables in the request, then
    * <i>BatchGetItem</i> will return a
    * <i>ProvisionedThroughputExceededException</i>. If <i>at least one</i> of
    * the items is successfully processed, then <i>BatchGetItem</i> completes
    * successfully, while returning the keys of the unread items in
    * <i>UnprocessedKeys</i>.
    * </p>
    * <important>
    * <p>
    * If DynamoDB returns any unprocessed items, you should retry the batch
    * operation on those items. However, <i>we strongly recommend that you use
    * an exponential backoff algorithm</i>. If you retry the batch operation
    * immediately, the underlying read or write requests can still fail due to
    * throttling on the individual tables. If you delay the batch operation
    * using exponential backoff, the individual requests in the batch are much
    * more likely to succeed.
    * </p>
    * <p>
    * For more information, see <a href=
    * "http://docs.aws.amazon.com/amazondynamodb/latest/developerguide/ErrorHandling.html#BatchOperations"
    * >Batch Operations and Error Handling</a> in the <i>Amazon DynamoDB
    * Developer Guide</i>.
    * </p>
    * </important>
    * <p>
    * By default, <i>BatchGetItem</i> performs eventually consistent reads on
    * every table in the request. If you want strongly consistent reads
    * instead, you can set <i>ConsistentRead</i> to <code>true</code> for any
    * or all tables.
    * </p>
    * <p>
    * In order to minimize response latency, <i>BatchGetItem</i> retrieves
    * items in parallel.
    * </p>
    * <p>
    * When designing your application, keep in mind that DynamoDB does not
    * return attributes in any particular order. To help parse the response by
    * item, include the primary key values for the items in your request in the
    * <i>AttributesToGet</i> parameter.
    * </p>
    * <p>
    * If a requested item does not exist, it is not returned in the result.
    * Requests for nonexistent items consume the minimum read capacity units
    * according to the type of read. For more information, see <a href=
    * "http://docs.aws.amazon.com/amazondynamodb/latest/developerguide/WorkingWithTables.html#CapacityUnitCalculations"
    * >Capacity Units Calculations</a> in the <i>Amazon DynamoDB Developer
    * Guide</i>.
    * </p>
    *
    * @param batchGetItemRequest
    * Represents the input of a <i>BatchGetItem</i> operation.
    * @return Result of the BatchGetItem operation returned by the service.
    * @throws ProvisionedThroughputExceededException
    * Your request rate is too high. The AWS SDKs for DynamoDB
    * automatically retry requests that receive this exception. Your
    * request is eventually successful, unless your retry queue is too
    * large to finish. Reduce the frequency of requests and use
    * exponential backoff. For more information, go to <a href=
    * "http://docs.aws.amazon.com/amazondynamodb/latest/developerguide/ErrorHandling.html#APIRetries"
    * >Error Retries and Exponential Backoff</a> in the <i>Amazon
    * DynamoDB Developer Guide</i>.
    * @throws ResourceNotFoundException
    * The operation tried to access a nonexistent table or index. The
    * resource might not be specified correctly, or its status might
    * not be <code>ACTIVE</code>.
    * @throws InternalServerErrorException
    * An error occurred on the server side.
    */
  def batchGetItem(batchGetItemRequest: BatchGetItemRequest): Future[BatchGetItemResult]

  /**
    * Simplified method form for invoking the BatchGetItem operation.
    *
    * @see #batchGetItem(BatchGetItemRequest)
    */
  def batchGetItem(requestItems: Map[String, KeysAndAttributes], returnConsumedCapacity: String): Future[BatchGetItemResult]

  /**
    * Simplified method form for invoking the BatchGetItem operation.
    *
    * @see #batchGetItem(BatchGetItemRequest)
    */
  def batchGetItem(requestItems: Map[String, KeysAndAttributes]): Future[BatchGetItemResult]

  /**
    * <p>
    * The <i>BatchWriteItem</i> operation puts or deletes multiple items in one
    * or more tables. A single call to <i>BatchWriteItem</i> can write up to 16
    * MB of data, which can comprise as many as 25 put or delete requests.
    * Individual items to be written can be as large as 400 KB.
    * </p>
    * <note>
    * <p>
    * <i>BatchWriteItem</i> cannot update items. To update items, use the
    * <i>UpdateItem</i> API.
    * </p>
    * </note>
    * <p>
    * The individual <i>PutItem</i> and <i>DeleteItem</i> operations specified
    * in <i>BatchWriteItem</i> are atomic; however <i>BatchWriteItem</i> as a
    * whole is not. If any requested operations fail because the table's
    * provisioned throughput is exceeded or an internal processing failure
    * occurs, the failed operations are returned in the <i>UnprocessedItems</i>
    * response parameter. You can investigate and optionally resend the
    * requests. Typically, you would call <i>BatchWriteItem</i> in a loop. Each
    * iteration would check for unprocessed items and submit a new
    * <i>BatchWriteItem</i> request with those unprocessed items until all
    * items have been processed.
    * </p>
    * <p>
    * Note that if <i>none</i> of the items can be processed due to
    * insufficient provisioned throughput on all of the tables in the request,
    * then <i>BatchWriteItem</i> will return a
    * <i>ProvisionedThroughputExceededException</i>.
    * </p>
    * <important>
    * <p>
    * If DynamoDB returns any unprocessed items, you should retry the batch
    * operation on those items. However, <i>we strongly recommend that you use
    * an exponential backoff algorithm</i>. If you retry the batch operation
    * immediately, the underlying read or write requests can still fail due to
    * throttling on the individual tables. If you delay the batch operation
    * using exponential backoff, the individual requests in the batch are much
    * more likely to succeed.
    * </p>
    * <p>
    * For more information, see <a href=
    * "http://docs.aws.amazon.com/amazondynamodb/latest/developerguide/ErrorHandling.html#BatchOperations"
    * >Batch Operations and Error Handling</a> in the <i>Amazon DynamoDB
    * Developer Guide</i>.
    * </p>
    * </important>
    * <p>
    * With <i>BatchWriteItem</i>, you can efficiently write or delete large
    * amounts of data, such as from Amazon Elastic MapReduce (EMR), or copy
    * data from another database into DynamoDB. In order to improve performance
    * with these large-scale operations, <i>BatchWriteItem</i> does not behave
    * in the same way as individual <i>PutItem</i> and <i>DeleteItem</i> calls
    * would. For example, you cannot specify conditions on individual put and
    * delete requests, and <i>BatchWriteItem</i> does not return deleted items
    * in the response.
    * </p>
    * <p>
    * If you use a programming language that supports concurrency, you can use
    * threads to write items in parallel. Your application must include the
    * necessary logic to manage the threads. With languages that don't support
    * threading, you must update or delete the specified items one at a time.
    * In both situations, <i>BatchWriteItem</i> provides an alternative where
    * the API performs the specified put and delete operations in parallel,
    * giving you the power of the thread pool approach without having to
    * introduce complexity into your application.
    * </p>
    * <p>
    * Parallel processing reduces latency, but each specified put and delete
    * request consumes the same number of write capacity units whether it is
    * processed in parallel or not. Delete operations on nonexistent items
    * consume one write capacity unit.
    * </p>
    * <p>
    * If one or more of the following is true, DynamoDB rejects the entire
    * batch write operation:
    * </p>
    * <ul>
    * <li>
    * <p>
    * One or more tables specified in the <i>BatchWriteItem</i> request does
    * not exist.
    * </p>
    * </li>
    * <li>
    * <p>
    * Primary key attributes specified on an item in the request do not match
    * those in the corresponding table's primary key schema.
    * </p>
    * </li>
    * <li>
    * <p>
    * You try to perform multiple operations on the same item in the same
    * <i>BatchWriteItem</i> request. For example, you cannot put and delete the
    * same item in the same <i>BatchWriteItem</i> request.
    * </p>
    * </li>
    * <li>
    * <p>
    * There are more than 25 requests in the batch.
    * </p>
    * </li>
    * <li>
    * <p>
    * Any individual item in a batch exceeds 400 KB.
    * </p>
    * </li>
    * <li>
    * <p>
    * The total request size exceeds 16 MB.
    * </p>
    * </li>
    * </ul>
    *
    * @param batchWriteItemRequest
    * Represents the input of a <i>BatchWriteItem</i> operation.
    * @return Result of the BatchWriteItem operation returned by the service.
    * @throws ProvisionedThroughputExceededException
    * Your request rate is too high. The AWS SDKs for DynamoDB
    * automatically retry requests that receive this exception. Your
    * request is eventually successful, unless your retry queue is too
    * large to finish. Reduce the frequency of requests and use
    * exponential backoff. For more information, go to <a href=
    * "http://docs.aws.amazon.com/amazondynamodb/latest/developerguide/ErrorHandling.html#APIRetries"
    * >Error Retries and Exponential Backoff</a> in the <i>Amazon
    * DynamoDB Developer Guide</i>.
    * @throws ResourceNotFoundException
    * The operation tried to access a nonexistent table or index. The
    * resource might not be specified correctly, or its status might
    * not be <code>ACTIVE</code>.
    * @throws ItemCollectionSizeLimitExceededException
    * An item collection is too large. This exception is only returned
    * for tables that have one or more local secondary indexes.
    * @throws InternalServerErrorException
    * An error occurred on the server side.
    */
  def batchWriteItem(batchWriteItemRequest: BatchWriteItemRequest): Future[BatchWriteItemResult]

  /**
    * Simplified method form for invoking the BatchWriteItem operation.
    *
    * @see #batchWriteItem(BatchWriteItemRequest)
    */
  def batchWriteItem(requestItems: Map[String, List[WriteRequest]]): Future[BatchWriteItemResult]

  /**
    * <p>
    * The <i>CreateTable</i> operation adds a new table to your account. In an
    * AWS account, table names must be unique within each region. That is, you
    * can have two tables with same name if you create the tables in different
    * regions.
    * </p>
    * <p>
    * <i>CreateTable</i> is an asynchronous operation. Upon receiving a
    * <i>CreateTable</i> request, DynamoDB immediately returns a response with
    * a <i>TableStatus</i> of <code>CREATING</code>. After the table is
    * created, DynamoDB sets the <i>TableStatus</i> to <code>ACTIVE</code>. You
    * can perform read and write operations only on an <code>ACTIVE</code>
    * table.
    * </p>
    * <p>
    * You can optionally define secondary indexes on the new table, as part of
    * the <i>CreateTable</i> operation. If you want to create multiple tables
    * with secondary indexes on them, you must create the tables sequentially.
    * Only one table with secondary indexes can be in the <code>CREATING</code>
    * state at any given time.
    * </p>
    * <p>
    * You can use the <i>DescribeTable</i> API to check the table status.
    * </p>
    *
    * @param createTableRequest
    * Represents the input of a <i>CreateTable</i> operation.
    * @return Result of the CreateTable operation returned by the service.
    * @throws ResourceInUseException
    *         The operation conflicts with the resource's availability. For
    *         example, you attempted to recreate an existing table, or tried to
    *         delete a table currently in the <code>CREATING</code> state.
    * @throws LimitExceededException
    *         The number of concurrent table requests (cumulative number of
    *         tables in the <code>CREATING</code>, <code>DELETING</code> or
    *         <code>UPDATING</code> state) exceeds the maximum allowed of
    *         10.</p>
    *         <p>
    *         Also, for tables with secondary indexes, only one of those tables
    *         can be in the <code>CREATING</code> state at any point in time.
    *         Do not attempt to create more than one such table simultaneously.
    *         </p>
    *         <p>
    *         The total limit of tables in the <code>ACTIVE</code> state is
    *         250.
    * @throws InternalServerErrorException
    *         An error occurred on the server side.
    */
  def createTable(createTableRequest: CreateTableRequest): Future[CreateTableResult]

  /**
    * Simplified method form for invoking the CreateTable operation.
    *
    * @see #createTable(CreateTableRequest)
    */
  def createTable(attributeDefinitions: List[AttributeDefinition], tableName: String, keySchema: List[KeySchemaElement], provisionedThroughput: ProvisionedThroughput): Future[CreateTableResult]

  /**
    * <p>
    * Deletes a single item in a table by primary key. You can perform a
    * conditional delete operation that deletes the item if it exists, or if it
    * has an expected attribute value.
    * </p>
    * <p>
    * In addition to deleting an item, you can also return the item's attribute
    * values in the same operation, using the <i>ReturnValues</i> parameter.
    * </p>
    * <p>
    * Unless you specify conditions, the <i>DeleteItem</i> is an idempotent
    * operation; running it multiple times on the same item or attribute does
    * <i>not</i> result in an error response.
    * </p>
    * <p>
    * Conditional deletes are useful for deleting items only if specific
    * conditions are met. If those conditions are met, DynamoDB performs the
    * delete. Otherwise, the item is not deleted.
    * </p>
    *
    * @param deleteItemRequest
    * Represents the input of a <i>DeleteItem</i> operation.
    * @return Result of the DeleteItem operation returned by the service.
    * @throws ConditionalCheckFailedException
    * A condition specified in the operation could not be evaluated.
    * @throws ProvisionedThroughputExceededException
    * Your request rate is too high. The AWS SDKs for DynamoDB
    * automatically retry requests that receive this exception. Your
    * request is eventually successful, unless your retry queue is too
    * large to finish. Reduce the frequency of requests and use
    * exponential backoff. For more information, go to <a href=
    * "http://docs.aws.amazon.com/amazondynamodb/latest/developerguide/ErrorHandling.html#APIRetries"
    * >Error Retries and Exponential Backoff</a> in the <i>Amazon
    * DynamoDB Developer Guide</i>.
    * @throws ResourceNotFoundException
    * The operation tried to access a nonexistent table or index. The
    * resource might not be specified correctly, or its status might
    * not be <code>ACTIVE</code>.
    * @throws ItemCollectionSizeLimitExceededException
    * An item collection is too large. This exception is only returned
    * for tables that have one or more local secondary indexes.
    * @throws InternalServerErrorException
    * An error occurred on the server side.
    */
  def deleteItem(deleteItemRequest: DeleteItemRequest): Future[DeleteItemResult]

  /**
    * Simplified method form for invoking the DeleteItem operation.
    *
    * @see #deleteItem(DeleteItemRequest)
    */
  def deleteItem(tableName: String, key: Map[String, AttributeValue]): Future[DeleteItemResult]

  /**
    * Simplified method form for invoking the DeleteItem operation.
    *
    * @see #deleteItem(DeleteItemRequest)
    */
  def deleteItem(tableName: String, key: Map[String, AttributeValue], returnValues: String): Future[DeleteItemResult]

  /**
    * <p>
    * The <i>DeleteTable</i> operation deletes a table and all of its items.
    * After a <i>DeleteTable</i> request, the specified table is in the
    * <code>DELETING</code> state until DynamoDB completes the deletion. If the
    * table is in the <code>ACTIVE</code> state, you can delete it. If a table
    * is in <code>CREATING</code> or <code>UPDATING</code> states, then
    * DynamoDB returns a <i>ResourceInUseException</i>. If the specified table
    * does not exist, DynamoDB returns a <i>ResourceNotFoundException</i>. If
    * table is already in the <code>DELETING</code> state, no error is
    * returned.
    * </p>
    * <note>
    * <p>
    * DynamoDB might continue to accept data read and write operations, such as
    * <i>GetItem</i> and <i>PutItem</i>, on a table in the
    * <code>DELETING</code> state until the table deletion is complete.
    * </p>
    * </note>
    * <p>
    * When you delete a table, any indexes on that table are also deleted.
    * </p>
    * <p>
    * If you have DynamoDB Streams enabled on the table, then the corresponding
    * stream on that table goes into the <code>DISABLED</code> state, and the
    * stream is automatically deleted after 24 hours.
    * </p>
    * <p>
    * Use the <i>DescribeTable</i> API to check the status of the table.
    * </p>
    *
    * @param deleteTableRequest
    * Represents the input of a <i>DeleteTable</i> operation.
    * @return Result of the DeleteTable operation returned by the service.
    * @throws ResourceInUseException
    *         The operation conflicts with the resource's availability. For
    *         example, you attempted to recreate an existing table, or tried to
    *         delete a table currently in the <code>CREATING</code> state.
    * @throws ResourceNotFoundException
    *         The operation tried to access a nonexistent table or index. The
    *         resource might not be specified correctly, or its status might
    *         not be <code>ACTIVE</code>.
    * @throws LimitExceededException
    *         The number of concurrent table requests (cumulative number of
    *         tables in the <code>CREATING</code>, <code>DELETING</code> or
    *         <code>UPDATING</code> state) exceeds the maximum allowed of
    *         10.</p>
    *         <p>
    *         Also, for tables with secondary indexes, only one of those tables
    *         can be in the <code>CREATING</code> state at any point in time.
    *         Do not attempt to create more than one such table simultaneously.
    *         </p>
    *         <p>
    *         The total limit of tables in the <code>ACTIVE</code> state is
    *         250.
    * @throws InternalServerErrorException
    *         An error occurred on the server side.
    */
  def deleteTable(deleteTableRequest: DeleteTableRequest): Future[DeleteTableResult]

  /**
    * Simplified method form for invoking the DeleteTable operation.
    *
    * @see #deleteTable(DeleteTableRequest)
    */
  def deleteTable(tableName: String): Future[DeleteTableResult]

  /**
    * <p>
    * Returns information about the table, including the current status of the
    * table, when it was created, the primary key schema, and any indexes on
    * the table.
    * </p>
    * <note>
    * <p>
    * If you issue a DescribeTable request immediately after a CreateTable
    * request, DynamoDB might return a ResourceNotFoundException. This is
    * because DescribeTable uses an eventually consistent query, and the
    * metadata for your table might not be available at that moment. Wait for a
    * few seconds, and then try the DescribeTable request again.
    * </p>
    * </note>
    *
    * @param describeTableRequest
    * Represents the input of a <i>DescribeTable</i> operation.
    * @return Result of the DescribeTable operation returned by the service.
    * @throws ResourceNotFoundException
    * The operation tried to access a nonexistent table or index. The
    * resource might not be specified correctly, or its status might
    * not be <code>ACTIVE</code>.
    * @throws InternalServerErrorException
    * An error occurred on the server side.
    */
  def describeTable(describeTableRequest: DescribeTableRequest): Future[DescribeTableResult]

  /**
    * Simplified method form for invoking the DescribeTable operation.
    *
    * @see #describeTable(DescribeTableRequest)
    */
  def describeTable(tableName: String): Future[DescribeTableResult]

  /**
    * <p>
    * The <i>GetItem</i> operation returns a set of attributes for the item
    * with the given primary key. If there is no matching item, <i>GetItem</i>
    * does not return any data.
    * </p>
    * <p>
    * <i>GetItem</i> provides an eventually consistent read by default. If your
    * application requires a strongly consistent read, set
    * <i>ConsistentRead</i> to <code>true</code>. Although a strongly
    * consistent read might take more time than an eventually consistent read,
    * it always returns the last updated value.
    * </p>
    *
    * @param getItemRequest
    * Represents the input of a <i>GetItem</i> operation.
    * @return Result of the GetItem operation returned by the service.
    * @throws ProvisionedThroughputExceededException
    * Your request rate is too high. The AWS SDKs for DynamoDB
    * automatically retry requests that receive this exception. Your
    * request is eventually successful, unless your retry queue is too
    * large to finish. Reduce the frequency of requests and use
    * exponential backoff. For more information, go to <a href=
    * "http://docs.aws.amazon.com/amazondynamodb/latest/developerguide/ErrorHandling.html#APIRetries"
    * >Error Retries and Exponential Backoff</a> in the <i>Amazon
    * DynamoDB Developer Guide</i>.
    * @throws ResourceNotFoundException
    * The operation tried to access a nonexistent table or index. The
    * resource might not be specified correctly, or its status might
    * not be <code>ACTIVE</code>.
    * @throws InternalServerErrorException
    * An error occurred on the server side.
    */
  def getItem(getItemRequest: GetItemRequest): Future[GetItemResult]

  /**
    * Simplified method form for invoking the GetItem operation.
    *
    * @see #getItem(GetItemRequest)
    */
  def getItem(tableName: String, key: Map[String, AttributeValue]): Future[GetItemResult]

  /**
    * Simplified method form for invoking the GetItem operation.
    *
    * @see #getItem(GetItemRequest)
    */
  def getItem(tableName: String, key: Map[String, AttributeValue], consistentRead: Boolean): Future[GetItemResult]

  /**
    * <p>
    * Returns an array of table names associated with the current account and
    * endpoint. The output from <i>ListTables</i> is paginated, with each page
    * returning a maximum of 100 table names.
    * </p>
    *
    * @param listTablesRequest
    * Represents the input of a <i>ListTables</i> operation.
    * @return Result of the ListTables operation returned by the service.
    * @throws InternalServerErrorException
    * An error occurred on the server side.
    */
  def listTables(listTablesRequest: ListTablesRequest): Future[ListTablesResult]

  /**
    * Simplified method form for invoking the ListTables operation.
    *
    * @see #listTables(ListTablesRequest)
    */
  def listTables: Future[ListTablesResult]

  /**
    * Simplified method form for invoking the ListTables operation.
    *
    * @see #listTables(ListTablesRequest)
    */
  def listTables(exclusiveStartTableName: String): Future[ListTablesResult]

  /**
    * Simplified method form for invoking the ListTables operation.
    *
    * @see #listTables(ListTablesRequest)
    */
  def listTables(exclusiveStartTableName: String, limit: Integer): Future[ListTablesResult]

  /**
    * Simplified method form for invoking the ListTables operation.
    *
    * @see #listTables(ListTablesRequest)
    */
  def listTables(limit: Integer): Future[ListTablesResult]

  /**
    * <p>
    * Creates a new item, or replaces an old item with a new item. If an item
    * that has the same primary key as the new item already exists in the
    * specified table, the new item completely replaces the existing item. You
    * can perform a conditional put operation (add a new item if one with the
    * specified primary key doesn't exist), or replace an existing item if it
    * has certain attribute values.
    * </p>
    * <p>
    * In addition to putting an item, you can also return the item's attribute
    * values in the same operation, using the <i>ReturnValues</i> parameter.
    * </p>
    * <p>
    * When you add an item, the primary key attribute(s) are the only required
    * attributes. Attribute values cannot be null. String and Binary type
    * attributes must have lengths greater than zero. Set type attributes
    * cannot be empty. Requests with empty values will be rejected with a
    * <i>ValidationException</i> exception.
    * </p>
    * <p>
    * You can request that <i>PutItem</i> return either a copy of the original
    * item (before the update) or a copy of the updated item (after the
    * update). For more information, see the <i>ReturnValues</i> description
    * below.
    * </p>
    * <note>
    * <p>
    * To prevent a new item from replacing an existing item, use a conditional
    * put operation with <i>ComparisonOperator</i> set to <code>NULL</code> for
    * the primary key attribute, or attributes.
    * </p>
    * </note>
    * <p>
    * For more information about using this API, see <a href=
    * "http://docs.aws.amazon.com/amazondynamodb/latest/developerguide/WorkingWithItems.html"
    * >Working with Items</a> in the <i>Amazon DynamoDB Developer Guide</i>.
    * </p>
    *
    * @param putItemRequest
    * Represents the input of a <i>PutItem</i> operation.
    * @return Result of the PutItem operation returned by the service.
    * @throws ConditionalCheckFailedException
    * A condition specified in the operation could not be evaluated.
    * @throws ProvisionedThroughputExceededException
    * Your request rate is too high. The AWS SDKs for DynamoDB
    * automatically retry requests that receive this exception. Your
    * request is eventually successful, unless your retry queue is too
    * large to finish. Reduce the frequency of requests and use
    * exponential backoff. For more information, go to <a href=
    * "http://docs.aws.amazon.com/amazondynamodb/latest/developerguide/ErrorHandling.html#APIRetries"
    * >Error Retries and Exponential Backoff</a> in the <i>Amazon
    * DynamoDB Developer Guide</i>.
    * @throws ResourceNotFoundException
    * The operation tried to access a nonexistent table or index. The
    * resource might not be specified correctly, or its status might
    * not be <code>ACTIVE</code>.
    * @throws ItemCollectionSizeLimitExceededException
    * An item collection is too large. This exception is only returned
    * for tables that have one or more local secondary indexes.
    * @throws InternalServerErrorException
    * An error occurred on the server side.
    */
  def putItem(putItemRequest: PutItemRequest): Future[PutItemResult]

  /**
    * Simplified method form for invoking the PutItem operation.
    *
    * @see #putItem(PutItemRequest)
    */
  def putItem(tableName: String, item: Map[String, AttributeValue]): Future[PutItemResult]

  /**
    * Simplified method form for invoking the PutItem operation.
    *
    * @see #putItem(PutItemRequest)
    */
  def putItem(tableName: String, item: Map[String, AttributeValue], returnValues: String): Future[PutItemResult]

  /**
    * <p>
    * A <i>Query</i> operation uses the primary key of a table or a secondary
    * index to directly access items from that table or index.
    * </p>
    * <p>
    * Use the <i>KeyConditionExpression</i> parameter to provide a specific
    * hash key value. The <i>Query</i> operation will return all of the items
    * from the table or index with that hash key value. You can optionally
    * narrow the scope of the <i>Query</i> operation by specifying a range key
    * value and a comparison operator in <i>KeyConditionExpression</i>. You can
    * use the <i>ScanIndexForward</i> parameter to get results in forward or
    * reverse order, by range key or by index key.
    * </p>
    * <p>
    * Queries that do not return results consume the minimum number of read
    * capacity units for that type of read operation.
    * </p>
    * <p>
    * If the total number of items meeting the query criteria exceeds the
    * result set size limit of 1 MB, the query stops and results are returned
    * to the user with the <i>LastEvaluatedKey</i> element to continue the
    * query in a subsequent operation. Unlike a <i>Scan</i> operation, a
    * <i>Query</i> operation never returns both an empty result set and a
    * <i>LastEvaluatedKey</i> value. <i>LastEvaluatedKey</i> is only provided
    * if the results exceed 1 MB, or if you have used the <i>Limit</i>
    * parameter.
    * </p>
    * <p>
    * You can query a table, a local secondary index, or a global secondary
    * index. For a query on a table or on a local secondary index, you can set
    * the <i>ConsistentRead</i> parameter to <code>true</code> and obtain a
    * strongly consistent result. Global secondary indexes support eventually
    * consistent reads only, so do not specify <i>ConsistentRead</i> when
    * querying a global secondary index.
    * </p>
    *
    * @param queryRequest
    * Represents the input of a <i>Query</i> operation.
    * @return Result of the Query operation returned by the service.
    * @throws ProvisionedThroughputExceededException
    * Your request rate is too high. The AWS SDKs for DynamoDB
    * automatically retry requests that receive this exception. Your
    * request is eventually successful, unless your retry queue is too
    * large to finish. Reduce the frequency of requests and use
    * exponential backoff. For more information, go to <a href=
    * "http://docs.aws.amazon.com/amazondynamodb/latest/developerguide/ErrorHandling.html#APIRetries"
    * >Error Retries and Exponential Backoff</a> in the <i>Amazon
    * DynamoDB Developer Guide</i>.
    * @throws ResourceNotFoundException
    * The operation tried to access a nonexistent table or index. The
    * resource might not be specified correctly, or its status might
    * not be <code>ACTIVE</code>.
    * @throws InternalServerErrorException
    * An error occurred on the server side.
    */
  def query(queryRequest: QueryRequest): Future[QueryResult]

  /**
    * <p>
    * The <i>Scan</i> operation returns one or more items and item attributes
    * by accessing every item in a table or a secondary index. To have DynamoDB
    * return fewer items, you can provide a <i>ScanFilter</i> operation.
    * </p>
    * <p>
    * If the total number of scanned items exceeds the maximum data set size
    * limit of 1 MB, the scan stops and results are returned to the user as a
    * <i>LastEvaluatedKey</i> value to continue the scan in a subsequent
    * operation. The results also include the number of items exceeding the
    * limit. A scan can result in no table data meeting the filter criteria.
    * </p>
    * <p>
    * By default, <i>Scan</i> operations proceed sequentially; however, for
    * faster performance on a large table or secondary index, applications can
    * request a parallel <i>Scan</i> operation by providing the <i>Segment</i>
    * and <i>TotalSegments</i> parameters. For more information, see <a href=
    * "http://docs.aws.amazon.com/amazondynamodb/latest/developerguide/QueryAndScan.html#QueryAndScanParallelScan"
    * >Parallel Scan</a> in the <i>Amazon DynamoDB Developer Guide</i>.
    * </p>
    * <p>
    * By default, <i>Scan</i> uses eventually consistent reads when acessing
    * the data in the table or local secondary index. However, you can use
    * strongly consistent reads instead by setting the <i>ConsistentRead</i>
    * parameter to <i>true</i>.
    * </p>
    *
    * @param scanRequest
    * Represents the input of a <i>Scan</i> operation.
    * @return Result of the Scan operation returned by the service.
    * @throws ProvisionedThroughputExceededException
    * Your request rate is too high. The AWS SDKs for DynamoDB
    * automatically retry requests that receive this exception. Your
    * request is eventually successful, unless your retry queue is too
    * large to finish. Reduce the frequency of requests and use
    * exponential backoff. For more information, go to <a href=
    * "http://docs.aws.amazon.com/amazondynamodb/latest/developerguide/ErrorHandling.html#APIRetries"
    * >Error Retries and Exponential Backoff</a> in the <i>Amazon
    * DynamoDB Developer Guide</i>.
    * @throws ResourceNotFoundException
    * The operation tried to access a nonexistent table or index. The
    * resource might not be specified correctly, or its status might
    * not be <code>ACTIVE</code>.
    * @throws InternalServerErrorException
    * An error occurred on the server side.
    */
  def scan(scanRequest: ScanRequest): Future[ScanResult]

  /**
    * Simplified method form for invoking the Scan operation.
    *
    * @see #scan(ScanRequest)
    */
  def scan(tableName: String, attributesToGet: List[String]): Future[ScanResult]

  /**
    * Simplified method form for invoking the Scan operation.
    *
    * @see #scan(ScanRequest)
    */
  def scan(tableName: String, scanFilter: Map[String, Condition]): Future[ScanResult]

  /**
    * Simplified method form for invoking the Scan operation.
    *
    * @see #scan(ScanRequest)
    */
  def scan(tableName: String, attributesToGet: List[String], scanFilter: Map[String, Condition]): Future[ScanResult]

  /**
    * <p>
    * Edits an existing item's attributes, or adds a new item to the table if
    * it does not already exist. You can put, delete, or add attribute values.
    * You can also perform a conditional update on an existing item (insert a
    * new attribute name-value pair if it doesn't exist, or replace an existing
    * name-value pair if it has certain expected attribute values). If
    * conditions are specified and the item does not exist, then the operation
    * fails and a new item is not created.
    * </p>
    * <p>
    * You can also return the item's attribute values in the same
    * <i>UpdateItem</i> operation using the <i>ReturnValues</i> parameter.
    * </p>
    *
    * @param updateItemRequest
    * Represents the input of an <i>UpdateItem</i> operation.
    * @return Result of the UpdateItem operation returned by the service.
    * @throws ConditionalCheckFailedException
    * A condition specified in the operation could not be evaluated.
    * @throws ProvisionedThroughputExceededException
    * Your request rate is too high. The AWS SDKs for DynamoDB
    * automatically retry requests that receive this exception. Your
    * request is eventually successful, unless your retry queue is too
    * large to finish. Reduce the frequency of requests and use
    * exponential backoff. For more information, go to <a href=
    * "http://docs.aws.amazon.com/amazondynamodb/latest/developerguide/ErrorHandling.html#APIRetries"
    * >Error Retries and Exponential Backoff</a> in the <i>Amazon
    * DynamoDB Developer Guide</i>.
    * @throws ResourceNotFoundException
    * The operation tried to access a nonexistent table or index. The
    * resource might not be specified correctly, or its status might
    * not be <code>ACTIVE</code>.
    * @throws ItemCollectionSizeLimitExceededException
    * An item collection is too large. This exception is only returned
    * for tables that have one or more local secondary indexes.
    * @throws InternalServerErrorException
    * An error occurred on the server side.
    */
  def updateItem(updateItemRequest: UpdateItemRequest): Future[UpdateItemResult]

  /**
    * Simplified method form for invoking the UpdateItem operation.
    *
    * @see #updateItem(UpdateItemRequest)
    */
  def updateItem(tableName: String, key: Map[String, AttributeValue], attributeUpdates: Map[String, AttributeValueUpdate]): Future[UpdateItemResult]

  /**
    * Simplified method form for invoking the UpdateItem operation.
    *
    * @see #updateItem(UpdateItemRequest)
    */
  def updateItem(tableName: String, key: Map[String, AttributeValue], attributeUpdates: Map[String, AttributeValueUpdate], returnValues: String): Future[UpdateItemResult]

  /**
    * <p>
    * Modifies the provisioned throughput settings, global secondary indexes,
    * or DynamoDB Streams settings for a given table.
    * </p>
    * <p>
    * You can only perform one of the following operations at once:
    * </p>
    * <ul>
    * <li>
    * <p>
    * Modify the provisioned throughput settings of the table.
    * </p>
    * </li>
    * <li>
    * <p>
    * Enable or disable Streams on the table.
    * </p>
    * </li>
    * <li>
    * <p>
    * Remove a global secondary index from the table.
    * </p>
    * </li>
    * <li>
    * <p>
    * Create a new global secondary index on the table. Once the index begins
    * backfilling, you can use <i>UpdateTable</i> to perform other operations.
    * </p>
    * </li>
    * </ul>
    * <p>
    * <i>UpdateTable</i> is an asynchronous operation; while it is executing,
    * the table status changes from <code>ACTIVE</code> to
    * <code>UPDATING</code>. While it is <code>UPDATING</code>, you cannot
    * issue another <i>UpdateTable</i> request. When the table returns to the
    * <code>ACTIVE</code> state, the <i>UpdateTable</i> operation is complete.
    * </p>
    *
    * @param updateTableRequest
    * Represents the input of an <i>UpdateTable</i> operation.
    * @return Result of the UpdateTable operation returned by the service.
    * @throws ResourceInUseException
    *         The operation conflicts with the resource's availability. For
    *         example, you attempted to recreate an existing table, or tried to
    *         delete a table currently in the <code>CREATING</code> state.
    * @throws ResourceNotFoundException
    *         The operation tried to access a nonexistent table or index. The
    *         resource might not be specified correctly, or its status might
    *         not be <code>ACTIVE</code>.
    * @throws LimitExceededException
    *         The number of concurrent table requests (cumulative number of
    *         tables in the <code>CREATING</code>, <code>DELETING</code> or
    *         <code>UPDATING</code> state) exceeds the maximum allowed of
    *         10.</p>
    *         <p>
    *         Also, for tables with secondary indexes, only one of those tables
    *         can be in the <code>CREATING</code> state at any point in time.
    *         Do not attempt to create more than one such table simultaneously.
    *         </p>
    *         <p>
    *         The total limit of tables in the <code>ACTIVE</code> state is
    *         250.
    * @throws InternalServerErrorException
    *         An error occurred on the server side.
    */
  def updateTable(updateTableRequest: UpdateTableRequest): Future[UpdateTableResult]

  /**
    * Simplified method form for invoking the UpdateTable operation.
    *
    * @see #updateTable(UpdateTableRequest)
    */
  def updateTable(tableName: String, provisionedThroughput: ProvisionedThroughput): Future[UpdateTableResult]

  /**
    * Shuts down this client object, releasing any resources that might be held
    * open. This is an optional method, and callers are not expected to call
    * it, but can if they want to explicitly release any open resources. Once a
    * client has been shutdown, it should not be used to make any more
    * requests.
    */
  def shutdown()

}

