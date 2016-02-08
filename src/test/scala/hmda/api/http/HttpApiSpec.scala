package hmda.api.http

import akka.event.{ NoLogging, LoggingAdapter }
import akka.http.scaladsl.testkit.ScalatestRouteTest
import hmda.api.model.Status
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import org.scalatest.{ MustMatchers, WordSpec }

class HttpApiSpec extends WordSpec with MustMatchers with ScalatestRouteTest with HttpApi {
  override val log: LoggingAdapter = NoLogging

  "Http API service" should {

    "return OK for GET requests to the root path" in {
      Get() ~> routes ~> check {
        responseAs[Status].status mustEqual "OK"
        responseAs[Status].service mustEqual "hmda-api"
      }
    }
  }
}
