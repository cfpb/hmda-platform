package hmda.api.http.institutions

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.server.MethodRejection
import hmda.api.http.InstitutionHttpApiSpec
import hmda.api.model.{ ErrorResponse, SubmissionStatusWrapper, SubmissionWrapper }
import hmda.model.fi._
import hmda.persistence.demo.DemoData

class SubmissionPathsSpec extends InstitutionHttpApiSpec {

  "Submission Paths" must {
    "find the latest submission for an institution" in {
      getWithCfpbHeaders("/institutions/0/filings/2017/submissions/latest") ~> institutionsRoutes ~> check {
        status mustBe StatusCodes.OK
        responseAs[SubmissionWrapper] mustBe SubmissionWrapper(3, SubmissionStatusWrapper(1, "created"))
      }
    }

    "return not found when looking for a latest submission for non existent institution" in {
      getWithCfpbHeaders("/institutions/xxxxx/filings/2017/submissions/latest") ~> institutionsRoutes ~> check {
        status mustBe StatusCodes.NotFound
        val error = ErrorResponse(404, "No submission found for xxxxx for 2017", "institutions/xxxxx/filings/2017/submissions/latest")
        responseAs[ErrorResponse] mustBe error
      }

    }

    "create a new submission" in {
      postWithCfpbHeaders("/institutions/0/filings/2017/submissions") ~> institutionsRoutes ~> check {
        status mustBe StatusCodes.Created
        val seqNr = DemoData.testSubmissions.size + 1
        responseAs[Submission] mustBe Submission(SubmissionId("0", "2017", seqNr), Created)
      }
    }

    "fail creating a new submission for a non existent institution" in {
      postWithCfpbHeaders("/institutions/xxxxx/filings/2017/submissions") ~> institutionsRoutes ~> check {
        status mustBe StatusCodes.NotFound
        responseAs[ErrorResponse] mustBe ErrorResponse(404, "Institution xxxxx not found", "institutions/xxxxx/filings/2017/submissions")
      }
    }

    "fail creating a new submission for a non existent filing period" in {
      postWithCfpbHeaders("/institutions/0/filings/2001/submissions") ~> institutionsRoutes ~> check {
        status mustBe StatusCodes.NotFound
        responseAs[ErrorResponse] mustBe ErrorResponse(404, "2001 filing not found for institution 0", "institutions/0/filings/2001/submissions")
      }
    }
  }

  "return 405 when trying to POST to the /latest endpoint" in {
    postWithCfpbHeaders("/institutions/0/filings/2017/submissions/latest") ~> institutionsRoutes ~> check {
      rejection mustBe a[MethodRejection]
    }
  }

}
