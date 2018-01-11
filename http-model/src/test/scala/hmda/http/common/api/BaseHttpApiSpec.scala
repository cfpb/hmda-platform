package hmda.http.common.api

import akka.event.{LoggingAdapter, NoLogging}
import org.scalatest.{MustMatchers, WordSpec}
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.http.scaladsl.model.headers.{HttpEncodings, `Accept-Encoding`}
import akka.http.scaladsl.model.headers.HttpEncodings._
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import hmda.http.common.model.Status
import io.circe.generic.auto._

import scala.concurrent.ExecutionContext

class BaseHttpApiSpec
    extends WordSpec
    with MustMatchers
    with ScalatestRouteTest
    with BaseHttpApi {
  override val log: LoggingAdapter = NoLogging
  val ec: ExecutionContext = system.dispatcher

  "Http API service" must {

    "return OK for GET requests to the root path" in {
      Get() ~> routes("hmda-filing-api") ~> check {
        responseAs[Status].status mustBe "OK"
        responseAs[Status].service mustBe "hmda-filing-api"
      }
    }

    "use requested encoding for root path" in {
      Get().addHeader(`Accept-Encoding`(gzip)) ~> routes("hmda-filing-api") ~> check {
        response.encoding mustBe HttpEncodings.gzip
      }
    }

  }
}
