package hmda.api.http

import akka.event.{ LoggingAdapter, NoLogging }
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import hmda.api.model.Status
import org.scalatest.{ MustMatchers, WordSpec }

import scala.concurrent.ExecutionContext

class HttpApiSpec extends WordSpec with MustMatchers with ScalatestRouteTest with BaseHttpApi {
  override val log: LoggingAdapter = NoLogging
  val ec: ExecutionContext = system.dispatcher

  "Http API service" must {

    "return OK for GET requests to the root path" in {
      Get() ~> routes("hmda-filing-api") ~> check {
        responseAs[Status].status mustBe "OK"
        responseAs[Status].service mustBe "hmda-filing-api"
      }
    }

  }

}
