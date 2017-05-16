import sbt._
import Keys._

object Dependencies {

  val awsVersion = "1.10.5.1"
  val httpAsyncVersion = "4.1.2"
  val scalaTestVersion = "3.0.1"
  val slf4jVersion = "1.6.4"
  val slf4jNop = "org.slf4j" % "slf4j-nop" % slf4jVersion

  val testDependencies: Seq[ModuleID] = Seq(
    "org.scalatest" %% "scalatest" % scalaTestVersion % Test
  )

  val core : Seq[ModuleID] = testDependencies ++ Seq(
    "com.amazonaws" % "aws-java-sdk-core" % awsVersion exclude("org.apache.httpcomponents", "httpasyncclient"),
    "org.apache.httpcomponents" % "httpasyncclient" % httpAsyncVersion,
    "io.exemplary.aws" % "aws-dynamodb-server" % awsVersion % Test
  )

  val dynamodb : Seq[ModuleID] = testDependencies ++ Seq(
    "com.amazonaws" % "aws-java-sdk-dynamodb" % awsVersion exclude("org.apache.httpcomponents", "httpasyncclient"),
    "org.apache.httpcomponents" % "httpasyncclient" % httpAsyncVersion,
    "io.exemplary.aws" % "aws-dynamodb-server" % awsVersion % Test
  )

}