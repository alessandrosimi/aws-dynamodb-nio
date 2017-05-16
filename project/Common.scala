import sbt._
import Keys._

object Common {

  val appName = "aws-nio"

  val settings: Seq[Def.Setting[_]] = Seq(
    version := Dependencies.awsVersion,
    organization := "io.exemplary.aws",
    scalaVersion := "2.11.11"
  )

}