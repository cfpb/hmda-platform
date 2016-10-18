package hmda.api.http.institutions.submissions

import akka.http.javadsl.model.StatusCodes
import hmda.api.http.InstitutionHttpApiSpec

class SubmissionIrsPathsSpec extends InstitutionHttpApiSpec {
  val supervisor = system.actorSelection("/user/supervisor")

  "Submission Irs Paths" must {
    "return a 202" in {
      getWithCfpbHeaders("/institutions/0/filings/2017/submissions/1/irs") ~> institutionsRoutes ~> check {
        status mustEqual StatusCodes.OK
      }
    }
  }
}
