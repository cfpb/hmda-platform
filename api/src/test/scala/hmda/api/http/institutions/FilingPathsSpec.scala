package hmda.api.http.institutions

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.StatusCodes
import hmda.api.http.{ InstitutionHttpApiSpec }
import hmda.api.model.{ ErrorResponse, FilingDetail }
import hmda.model.fi.{ Filing, NotStarted }
import hmda.persistence.demo.DemoData

class FilingPathsSpec extends InstitutionHttpApiSpec with FilingPaths {

  "return a list of filings for a financial institution" in {
    getWithCfpbHeaders("/institutions/0/filings/2017") ~> institutionsRoutes ~> check {
      status mustBe StatusCodes.OK
      val filing = Filing("2017", "0", NotStarted)
      responseAs[FilingDetail] mustBe FilingDetail(filing, DemoData.testSubmissions.reverse)
    }

    getWithCfpbHeaders("/institutions/0/filings/xxxx") ~> institutionsRoutes ~> check {
      status mustBe StatusCodes.NotFound
      responseAs[ErrorResponse] mustBe ErrorResponse(404, "xxxx filing not found for institution 0", "institutions/0/filings/xxxx")
    }
    getWithCfpbHeaders("/institutions/xxxxx/filings/2017") ~> institutionsRoutes ~> check {
      status mustBe StatusCodes.NotFound
      responseAs[ErrorResponse] mustBe ErrorResponse(404, "Institution xxxxx not found", "institutions/xxxxx/filings/2017")
    }
  }
}
