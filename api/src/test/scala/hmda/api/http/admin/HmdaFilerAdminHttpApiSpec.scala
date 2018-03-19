package hmda.api.http.admin

import akka.event.{ LoggingAdapter, NoLogging }
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.util.Timeout
import org.scalatest.{ MustMatchers, WordSpec }

import scala.concurrent.duration._
import hmda.model.institution.FilingGenerators._
import hmda.persistence.HmdaSupervisor._
import hmda.validation.stats.ValidationStats._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import hmda.model.institution.HmdaFiler

class HmdaFilerAdminHttpApiSpec extends WordSpec with MustMatchers with ScalatestRouteTest with HmdaFilerAdminHttpApi {

  val duration = 10.seconds
  override implicit val timeout: Timeout = Timeout(duration)
  override val log: LoggingAdapter = NoLogging

  val validationStats = createValidationStats(system)
  val supervisor = createSupervisor(system, validationStats)

  val hmdaFiler = hmdaFilerGen.sample.getOrElse(HmdaFiler("id", "respId", "2017", "bank"))

  "HMDA Filer admin" must {
    "create, read and delete" in {
      Post("/filers", hmdaFiler) ~> hmdaFilerAdminRoutes(supervisor) ~> check {
        status mustBe StatusCodes.Created
        responseAs[HmdaFiler] mustBe hmdaFiler
      }
      Get("/filers") ~> hmdaFilerAdminRoutes(supervisor) ~> check {
        status mustBe StatusCodes.OK
        responseAs[Set[HmdaFiler]] mustBe Set(hmdaFiler)
      }
      Delete("/filers", hmdaFiler) ~> hmdaFilerAdminRoutes(supervisor) ~> check {
        status mustBe StatusCodes.Accepted
        responseAs[HmdaFiler] mustBe hmdaFiler
      }
    }
  }

}
