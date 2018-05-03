package hmda.api.http

import akka.event.{LoggingAdapter, NoLogging}
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.headers.{HttpEncodings, `Accept-Encoding`}
import akka.http.scaladsl.model.headers.HttpEncodings._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import hmda.api.http.model.common.HmdaServiceStatus
import org.scalatest.{MustMatchers, WordSpec}
import io.circe.generic.auto._
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._

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
        val serviceStatus = responseAs[HmdaServiceStatus]
        serviceStatus.service mustBe "hmda-public-api"
        serviceStatus.status mustBe "OK"
      }
    }

    "use requested encoding for root path" in {
      Get().addHeader(`Accept-Encoding`(gzip)) ~> routes("hmda-public-api") ~> check {
        response.encoding mustBe HttpEncodings.gzip
      }
    }
  }
}
