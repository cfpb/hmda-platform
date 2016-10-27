package hmda.api.http

import akka.event.{ NoLogging, LoggingAdapter }
import akka.http.scaladsl.model._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import hmda.api.model.Status
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import org.scalatest.{ MustMatchers, WordSpec }

import scala.concurrent.ExecutionContext

class HttpApiSpec extends WordSpec with MustMatchers with ScalatestRouteTest with HttpApi {
  override val log: LoggingAdapter = NoLogging
  val ec: ExecutionContext = system.dispatcher

  "Http API service" must {

    "return OK for GET requests to the root path" in {
      Get() ~> routes ~> check {
        responseAs[Status].status mustBe "OK"
        responseAs[Status].service mustBe "hmda-api"
      }
    }

  }

}
