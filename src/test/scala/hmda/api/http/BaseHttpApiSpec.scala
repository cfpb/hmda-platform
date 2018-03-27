package hmda.api.http

import akka.event.{LoggingAdapter, NoLogging}
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.{MustMatchers, WordSpec}

import scala.concurrent.ExecutionContext

class BaseHttpApiSpec
    extends WordSpec
    with MustMatchers
    with ScalatestRouteTest
    with BaseHttpApi {
  override val log: LoggingAdapter = NoLogging
  val ec: ExecutionContext = system.dispatcher

  "Http API Service" must {
    "return OK for GET requests to the root path" in {
      Get() ~> routes("hmda-public-api") ~> check {
        response.status mustBe StatusCodes.OK
      }
    }
  }
}
