package hmda.api.http.public

import akka.actor.ActorRef
import akka.event.{ LoggingAdapter, NoLogging }
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.{ MissingQueryParamRejection, Route }
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.util.Timeout
import org.scalatest.{ BeforeAndAfterAll, MustMatchers, WordSpec }
import hmda.query.view.institutions.InstitutionView._

import scala.concurrent.duration._
import scala.concurrent.{ ExecutionContext, Future }

class InstitutionSearchPathSpec extends WordSpec with MustMatchers with BeforeAndAfterAll
    with ScalatestRouteTest with InstitutionSearchPaths {

  override implicit val timeout: Timeout = Timeout(10.seconds)
  override val ec: ExecutionContext = system.dispatcher
  override val log: LoggingAdapter = NoLogging

  val institutionViewF: Future[ActorRef] = Future(createInstitutionView(system))

  "Institution Search" must {
    "return not found when email domain not present" in {
      Get("/institutions?domain=xxxxx") ~> institutionSearchPath(institutionViewF) ~> check {
        status mustBe StatusCodes.NotFound
      }
    }
    "return error when domain parameter is not present" in {
      Get("/institutions") ~> Route.seal(institutionSearchPath(institutionViewF)) ~> check {
        status mustBe StatusCodes.NotFound
        responseAs[String] mustBe "Request is missing required query parameter 'domain'"
      }
    }
  }

}
