package hmda.api.http.institutions.submissions

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.Uri.Path
import akka.http.scaladsl.server.MethodRejection
import hmda.api.http.InstitutionHttpApiSpec
import hmda.api.model._
import hmda.model.fi._
import hmda.persistence.demo.DemoData

class SubmissionBasePathsSpec extends InstitutionHttpApiSpec {

  "Submission Paths" must {
    "return not found when looking for a latest submission for non existent institution" in {
      val path = Path("/institutions/xxxxx/filings/2017/submissions/latest")
      getWithCfpbHeaders(path.toString) ~> institutionsRoutes ~> check {
        status mustBe StatusCodes.NotFound
        val error = ErrorResponse(404, "No submission found for xxxxx for 2017", path)
        responseAs[ErrorResponse] mustBe error
      }

    }

    "create a new submission" in {
      postWithCfpbHeaders("/institutions/0/filings/2017/submissions") ~> institutionsRoutes ~> check {
        status mustBe StatusCodes.Created
        responseAs[Submission].status mustBe Created
      }
    }

    "find the latest submission for an institution" in {
      getWithCfpbHeaders("/institutions/0/filings/2017/submissions/latest") ~> institutionsRoutes ~> check {
        status mustBe StatusCodes.OK
        responseAs[Submission].status mustBe Created
      }
    }

    "fail creating a new submission for a non existent institution" in {
      val path: Path = Path("/institutions/xxxxx/filings/2017/submissions")
      postWithCfpbHeaders(path.toString) ~> institutionsRoutes ~> check {
        status mustBe StatusCodes.NotFound
        responseAs[ErrorResponse] mustBe ErrorResponse(404, "Institution xxxxx not found", path)
      }
    }

    "fail creating a new submission for a non existent filing period" in {
      val path = Path("/institutions/0/filings/2001/submissions")
      postWithCfpbHeaders(path.toString) ~> institutionsRoutes ~> check {
        status mustBe StatusCodes.NotFound
        responseAs[ErrorResponse] mustBe ErrorResponse(404, "2001 filing not found for institution 0", path)
      }
    }

    "return 405 when trying to POST to the /latest endpoint" in {
      postWithCfpbHeaders("/institutions/0/filings/2017/submissions/latest") ~> institutionsRoutes ~> check {
        rejection mustBe a[MethodRejection]
      }
    }
  }

}
