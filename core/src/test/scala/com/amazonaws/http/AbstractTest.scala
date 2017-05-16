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
package com.amazonaws.http

import io.exemplary.aws.DynamoDBServer
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, FeatureSpec, Matchers}

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

abstract class AbstractTest extends FeatureSpec with BeforeAndAfterAll with BeforeAndAfter with Matchers {

  def resultOf[A](futureResult: Future[A]): A = Await.result(futureResult, 1.minute)

  val server = new DynamoDBServer()

  override def beforeAll(): Unit = {
    server.start()
  }

  before {
    server.doesNotFail()
  }

  override def afterAll(): Unit = {
    server.stop()
  }

}
