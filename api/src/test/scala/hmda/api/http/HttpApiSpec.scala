package hmda.api.http

import akka.event.{ LoggingAdapter, NoLogging }
import akka.http.scaladsl.testkit.ScalatestRouteTest
import hmda.api.model.Status
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.util.Timeout
import org.scalatest.{ MustMatchers, WordSpec }

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

class HttpApiSpec extends WordSpec with MustMatchers with ScalatestRouteTest with HttpApi {
  override val log: LoggingAdapter = NoLogging
  override implicit val timeout: Timeout = Timeout(5.seconds)
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
