package hmda.api.http.routes

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.headers.HttpEncodings._
import akka.http.scaladsl.model.headers._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import hmda.api.http.model.HmdaServiceStatus
import hmda.api.http.routes.BaseHttpApi._
import io.circe.generic.auto._
import org.scalatest.{ MustMatchers, WordSpec }
import org.slf4j.{ Logger, LoggerFactory }

import scala.concurrent.ExecutionContext

class BaseHttpApiSpec extends WordSpec with MustMatchers with ScalatestRouteTest {
  val log: Logger          = LoggerFactory.getLogger(getClass)
  val ec: ExecutionContext = system.dispatcher

  "Http API Service" must {
    "return OK for GET requests to the root path" in {
      Get() ~> BaseHttpApi.routes("hmda-public-api") ~> check {
        response.status mustBe StatusCodes.OK
        val serviceStatus = responseAs[HmdaServiceStatus]
        serviceStatus.service mustBe "hmda-public-api"
        serviceStatus.status mustBe "OK"
      }
    }

    "use requested encoding for root path" in {
      Get().addHeader(`Accept-Encoding`(gzip)) ~> BaseHttpApi.routes("hmda-public-api") ~> check {
        response.encoding mustBe HttpEncodings.gzip
      }
    }
    "Allow CORS requests" in {
      Get() ~> Origin(HttpOrigin("http://ffiec.cfpb.gov")) ~> routes("hmda-public-api") ~> check {
        response.status mustBe StatusCodes.OK
        val serviceStatus = responseAs[HmdaServiceStatus]
        serviceStatus.service mustBe "hmda-public-api"
        serviceStatus.status mustBe "OK"
      }
    }
  }
}