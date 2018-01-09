package hmda.api.http.admin

import java.time.LocalDate

import akka.event.{ LoggingAdapter, NoLogging }
import akka.http.scaladsl.model._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.util.{ ByteString, Timeout }
import hmda.model.apor.APORGenerator.APORGen
import hmda.persistence.HmdaSupervisor
import hmda.validation.stats.ValidationStats
import org.scalatest.{ BeforeAndAfterAll, MustMatchers, WordSpec }
import hmda.api.model.admin.AdminAporRequests.{ CreateAporRequest, ModifyAporRequest }
import hmda.model.apor.{ APOR, FixedRate }
import hmda.persistence.messages.events.apor.APOREvents.{ AporCreated, AporModified }
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import hmda.api.protocol.admin.AdminAporProtocol._
import hmda.api.protocol.apor.RateSpreadProtocol._
import spray.json._

import scala.concurrent.duration._

class APORAdminHttpApiSpec extends WordSpec with MustMatchers with ScalatestRouteTest with APORAdminHttpApi with BeforeAndAfterAll {

  override implicit val timeout: Timeout = Timeout(10.seconds)
  override val log: LoggingAdapter = NoLogging

  val validationStats = ValidationStats.createValidationStats(system)
  val supervisor = HmdaSupervisor.createSupervisor(system, validationStats)

  val date = LocalDate.of(2018, 1, 8)

  val apor1 = APORGen.sample.get.copy(rateDate = date)
  val apor2 = APORGen.sample.get.copy(rateDate = date)

  override def afterAll(): Unit = {
    super.afterAll()
    system.terminate()
  }

  "APOR Admin Paths" must {
    "Create new APOR" in {
      val createAporJsonRequest = ByteString(CreateAporRequest(apor1, FixedRate).toJson.toString)
      val postRequest = createRequest(createAporJsonRequest, HttpMethods.POST)
      postRequest ~> aporRoutes(supervisor) ~> check {
        status mustBe StatusCodes.Created
        responseAs[AporCreated] mustBe AporCreated(apor1, FixedRate)
      }
    }
    "Modify APOR" in {
      val modifyAporJsonRequest = ByteString(ModifyAporRequest(apor2, FixedRate).toJson.toString)
      val putRequest = createRequest(modifyAporJsonRequest, HttpMethods.PUT)
      putRequest ~> aporRoutes(supervisor) ~> check {
        status mustBe StatusCodes.Accepted
        responseAs[AporModified] mustBe AporModified(apor2, FixedRate)
      }
    }
    "Find APOR by date" in {
      Get("/apor/fixed/2018/1/8") ~> aporRoutes(supervisor) ~> check {
        status mustBe StatusCodes.OK
        responseAs[APOR] mustBe apor2
      }
    }
  }

  private def createRequest(jsonRequest: ByteString, method: HttpMethod): HttpRequest = {
    HttpRequest(
      method,
      uri = "/apor",
      entity = HttpEntity(MediaTypes.`application/json`, jsonRequest)
    )
  }

}
