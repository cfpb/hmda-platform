package hmda.api.http

import akka.event.{ LoggingAdapter, NoLogging }
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.util.Timeout
import hmda.api.util.TestData
import hmda.api.model.processing.{ Institution, Institutions }
import hmda.api.processing.submission.InstitutionsFiling._
import org.scalatest.{ BeforeAndAfterAll, MustMatchers, WordSpec }

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext

class ProcessingHttpApiSpec extends WordSpec with MustMatchers with ScalatestRouteTest with ProcessingHttpApi with BeforeAndAfterAll {

  override val log: LoggingAdapter = NoLogging
  val ec: ExecutionContext = system.dispatcher
  override implicit val timeout: Timeout = Timeout(5.seconds)

  override def beforeAll(): Unit = {
    createInstitutionsFiling(system)
    TestData.loadData(system)
  }

  "Processing HTTP service" must {
    "get list of institutions filing or filed for a specific period" in {
      val inst18 = Institution().copy(period = "2018")
      val institutionsActor = system.actorSelection("/user/institutions")
      institutionsActor ! CreateInstitution(inst18)

      Get("/institutions/period/2018") ~> processingRoutes ~> check {
        status mustBe StatusCodes.OK
        responseAs[Institutions] mustBe (Institutions(Set(inst18)))
      }

      Get("/institutions/period/2017") ~> processingRoutes ~> check {
        status mustBe StatusCodes.OK
        responseAs[Institutions] mustBe (Institutions(TestData.institutions))
      }
    }
  }

}
