**Latest release:** 1.10.5.1<br/>
**License:** [Apache 2.0](http://www.apache.org/licenses/LICENSE-2.0)

# Aws DynamoDB Non-Blocking IO Client

Scala dynamoDB client built on top of Apache [async http client](https://hc.apache.org/httpcomponents-asyncclient-dev/)
with almost the same API provided by [AWS SKD for Java(tm)](https://aws.amazon.com/it/sdk-for-java/).

## Why Non-Blocking IO?

Aws DynamoDB NIO Client compared with other clients uses Non-Blocking IO, so no threads are blocked during the call to Aws services.
Other async clients use the AWS SKD for Java that is blocking while Aws DynamoDB NIO Client re-implements the SKD with Apache async http client (NIO).

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

The `NIO` client API differs from the original Amazon Api in two points: it needs an implicit execution context to run it and the `setRegion(...)` and `setEndpoint(...)`
methods return a new client with the value changed.

## How to get it

You can add DynamoNio client as a maven dependency for scala 2.11 ...

```xml
<dependency>
    <groupId>io.exemplary.aws</groupId>
    <artifactId>aws-dynamodb-nio_2.11</artifactId>
    <version>${aws.version}</version>
</dependency>
```
... or for scala 2.12 ..

```xml
<dependency>
    <groupId>io.exemplary.aws</groupId>
    <artifactId>aws-dynamodb-nio_2.12</artifactId>
    <version>${aws.version}</version>
</dependency>
```

The current supported AWS vesion is `1.10.5.1`.
