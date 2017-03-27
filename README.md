# Aws DynamoDB Non-Blocking IO Client

Scala dynamoDB client built on top of Apache [async http client](https://hc.apache.org/httpcomponents-asyncclient-dev/)
with almost the same API provided by [AWS SKD for Java(tm)](https://aws.amazon.com/it/sdk-for-java/)

## Easy to use

The client is easy to use especially if you are familiar with the official AWS api.

```scala
val blockingClient = new AmazonDynamoDBClient()
val tableList: ListTablesResult = blockingClient.listTables
println(tableList.getTableNames)

val nioClient = new AmazonDynamoDBNioClient
val futureTableList: Future[ListTablesResult] = nioClient.listTables
futureTableList
  .map(tableList => tableList.getTableNames)
  .foreach(println)
```

The `NIO` client differs for only two aspects. First it needs an implicit excecution context to run it and second, the `setRegion(...)` and `setEndpoint(...)`
methods return a new client with the value changed.

## How to get it

The client would be soon available directly from maven.

```xml
<dependency>
    <groupId>com.as.aws</groupId>
    <artifactId>aws-dynamodb-nio_2.11</artifactId>
    <version>${aws.version}</version>
</dependency>
```
