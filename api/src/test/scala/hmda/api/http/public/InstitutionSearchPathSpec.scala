package hmda.api.http.public

import akka.actor.ActorRef
import akka.event.{ LoggingAdapter, NoLogging }
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.util.Timeout
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.Uri.Path
import hmda.api.model.ErrorResponse
import hmda.api.model.public.InstitutionSearchResults
import hmda.model.institution.{ Institution, InstitutionGenerators }
import hmda.persistence.messages.events.institutions.InstitutionEvents.InstitutionCreated
import hmda.persistence.processing.HmdaQuery.EventWithSeqNr
import org.scalatest.{ BeforeAndAfterAll, MustMatchers, WordSpec }
import hmda.query.view.institutions.InstitutionView._

import scala.concurrent.duration._
import scala.concurrent.{ Await, ExecutionContext, Future }

class InstitutionSearchPathSpec extends WordSpec with MustMatchers with BeforeAndAfterAll
    with ScalatestRouteTest with InstitutionSearchPaths {

  val duration = 10.seconds
  override implicit val timeout: Timeout = Timeout(duration)
  override val ec: ExecutionContext = system.dispatcher
  override val log: LoggingAdapter = NoLogging

  val institutionViewF: Future[ActorRef] = Future(createInstitutionView(system))

  val i0 = InstitutionGenerators.sampleInstitution.copy(emailDomains = Set("bank0.com", "", ""))
  val i1 = InstitutionGenerators.sampleInstitution.copy(emailDomains = Set("test@bank1.com", "", ""))

  override def beforeAll(): Unit = {
    super.beforeAll()
    val institutionView = Await.result(institutionViewF, duration)
    institutionView ! EventWithSeqNr(1, InstitutionCreated(i0))
    institutionView ! EventWithSeqNr(2, InstitutionCreated(i1))
  }

  "Institution Search" must {
    "return not found when email domain not present" in {
      Get("/institutions?domain=xxxxx") ~> institutionSearchPath(institutionViewF) ~> check {
        status mustBe StatusCodes.NotFound
        responseAs[ErrorResponse] mustBe ErrorResponse(404, s"email domain xxxxx not found", Path("/institutions"))
      }
    }
    "return error when domain parameter is not present" in {
      Get("/institutions") ~> Route.seal(institutionSearchPath(institutionViewF)) ~> check {
        status mustBe StatusCodes.NotFound
        responseAs[String] mustBe "Request is missing required query parameter 'domain'"
      }
    }
    "return filtered institution by email domain" in {
      Get("/institutions?domain=bank0.com") ~> institutionSearchPath(institutionViewF) ~> check {
        status mustBe StatusCodes.OK
        responseAs[InstitutionSearchResults].institutions.size mustBe 1
        responseAs[InstitutionSearchResults].institutions.head mustBe institutiontoInstitutionSearch(i0)
      }
    }
  }

}
