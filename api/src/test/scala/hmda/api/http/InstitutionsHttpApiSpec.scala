package hmda.api.http

import akka.event.{ LoggingAdapter, NoLogging }
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.util.Timeout
import hmda.api.persistence.InstitutionPersistence._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import hmda.api.demo.DemoData
import hmda.api.model._
import hmda.model.fi._
import org.scalatest.{ BeforeAndAfterAll, MustMatchers, WordSpec }

import scala.concurrent.duration._

class InstitutionsHttpApiSpec extends WordSpec with MustMatchers with ScalatestRouteTest with InstitutionsHttpApi with BeforeAndAfterAll {
  override val log: LoggingAdapter = NoLogging
  override implicit val timeout: Timeout = Timeout(5.seconds)

  val ec = system.dispatcher

  override def beforeAll(): Unit = {
    createInstitutions(system)

    DemoData.loadData(system)
  }

  "Institutions HTTP API" must {
    "return a list of existing institutions" in {
      Get("/institutions") ~> institutionsRoutes ~> check {
        status mustBe StatusCodes.OK
        responseAs[Institutions] mustBe Institutions(DemoData.institutions)
      }
    }

    "return an institution by id" in {
      Get("/institutions/12345") ~> institutionsRoutes ~> check {
        status mustBe StatusCodes.OK
        val institution = DemoData.institutions.head
        val filings = DemoData.filings.filter(f => f.fid == institution.id).reverse
        responseAs[InstitutionDetail] mustBe InstitutionDetail(institution, filings)
      }
    }

    "return an institution's summary" in {
      Get("/institutions/12345/summary") ~> institutionsRoutes ~> check {
        status mustBe StatusCodes.OK
        responseAs[InstitutionSummary] mustBe DemoData.institutionSummary
      }
    }

    "return a list of submissions for a financial institution" in {
      Get("/institutions/12345/filings/2017") ~> institutionsRoutes ~> check {
        status mustBe StatusCodes.OK
        val filing = Filing("2017", "12345", NotStarted)
        responseAs[FilingDetail] mustBe FilingDetail(filing, DemoData.newSubmissions.reverse)
      }
    }

    "create a new submission" in {
      Post("/institutions/12345/filings/2017/submissions") ~> institutionsRoutes ~> check {
        status mustBe StatusCodes.Created
        responseAs[Submission] mustBe Submission(DemoData.newSubmissions.size + 1, Created)
      }
    }
  }

}
