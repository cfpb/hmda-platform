package hmda.api.http.institutions

import akka.event.{ LoggingAdapter, NoLogging }
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.util.Timeout
import hmda.api.http.{ InstitutionSpec }
import hmda.api.model._
import hmda.model.institution.Institution
import hmda.persistence.demo.DemoData

import scala.concurrent.duration._

class InstitutionsPathsSpec extends InstitutionSpec {

  override val log: LoggingAdapter = NoLogging
  override implicit val timeout: Timeout = Timeout(5.seconds)

  val ec = system.dispatcher

  "Institutions Paths" must {
    "return a list of institutions matching the 'CFPB-HMDA-Institutions' header" in {
      val i1 = DemoData.testInstitutions.find(i => i.id == "1").get
      val i2 = DemoData.testInstitutions.find(i => i.id == "2").get
      val institutions: Set[Institution] = Set(i1, i2)
      val institutionsWrapped = institutions.map(i => InstitutionWrapper(i.id.toString, i.name, i.status))

      Get("/institutions")
        .addHeader(usernameHeader)
        .addHeader(RawHeader("CFPB-HMDA-Institutions", "1,2")) ~> institutionsRoutes ~> check {
          status mustBe StatusCodes.OK
          responseAs[Institutions] mustBe Institutions(institutionsWrapped)
        }
    }

    "return an institution by id" in {
      getWithCfpbHeaders("/institutions/0") ~> institutionsRoutes ~> check {
        status mustBe StatusCodes.OK
        val institution = DemoData.testInstitutions.head
        val institutionWrapped = InstitutionWrapper(institution.id.toString, institution.name, institution.status)
        val filings = DemoData.testFilings.filter(f => f.institutionId == institution.id.toString)
        responseAs[InstitutionDetail] mustBe InstitutionDetail(institutionWrapped, filings.reverse)
      }
      getWithCfpbHeaders("/institutions/xxxxx") ~> institutionsRoutes ~> check {
        status mustBe StatusCodes.NotFound
        responseAs[ErrorResponse] mustBe ErrorResponse(404, "Institution xxxxx not found", "institutions/xxxxx")
      }
    }

    "return an institution's summary" in {
      getWithCfpbHeaders("/institutions/0/summary") ~> institutionsRoutes ~> check {
        status mustBe StatusCodes.OK
        val summary = DemoData.institutionSummary
        val institutionSummary = InstitutionSummary(summary._1.toString, summary._2, summary._3)
        responseAs[InstitutionSummary] mustBe institutionSummary
      }
    }
  }

}
