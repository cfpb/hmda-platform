package hmda.api.http.institutions.submissions

import akka.http.javadsl.model.StatusCodes
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import hmda.api.http.InstitutionHttpApiSpec
import hmda.api.model.Receipt
import spray.json.{ JsBoolean, JsObject }

class SubmissionIrsPathsSpec extends InstitutionHttpApiSpec {
  val supervisor = system.actorSelection("/user/supervisor")

  "Submission Irs Paths" must {
    "return a 200" in {
      getWithCfpbHeaders("/institutions/0/filings/2017/submissions/1/irs") ~> institutionsRoutes ~> check {
        status mustBe StatusCodes.OK
      }
    }

    "return a filled receipt" in {
      val verified = JsObject("verified" -> JsBoolean(true))

      postWithCfpbHeaders("/institutions/0/filings/2017/submissions/1/irs", verified) ~> institutionsRoutes ~> check {
        status mustBe StatusCodes.OK
        responseAs[Receipt].receipt mustBe "receiptHash"
      }
    }

    "return an empty receipt" in {
      val verified = JsObject("verified" -> JsBoolean(false))

      postWithCfpbHeaders("/institutions/0/filings/2017/submissions/1/irs", verified) ~> institutionsRoutes ~> check {
        status mustBe StatusCodes.OK
        responseAs[Receipt] mustBe Receipt.empty
      }
    }
  }
}
