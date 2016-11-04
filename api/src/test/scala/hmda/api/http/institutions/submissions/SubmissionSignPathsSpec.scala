package hmda.api.http.institutions.submissions

import akka.http.javadsl.model.StatusCodes
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import hmda.api.http.InstitutionHttpApiSpec
import hmda.api.model.Receipt
import hmda.model.fi.IRSVerified
import spray.json.{ JsBoolean, JsObject }

class SubmissionSignPathsSpec extends InstitutionHttpApiSpec {
  val supervisor = system.actorSelection("/user/supervisor")

  "Submission Sign Paths" must {
    "return a 200" in {
      getWithCfpbHeaders("/institutions/0/filings/2017/submissions/1/sign") ~> institutionsRoutes ~> check {
        status mustBe StatusCodes.OK
        responseAs[Receipt].status mustBe IRSVerified
      }
    }

    "return a filled receipt" in {
      val signed = JsObject("signed" -> JsBoolean(true))

      postWithCfpbHeaders("/institutions/0/filings/2017/submissions/1/sign", signed) ~> institutionsRoutes ~> check {
        status mustBe StatusCodes.OK
        responseAs[Receipt].receipt mustBe "receiptHash"
      }
    }

    "return an empty receipt" in {
      val signed = JsObject("signed" -> JsBoolean(false))

      postWithCfpbHeaders("/institutions/0/filings/2017/submissions/1/sign", signed) ~> institutionsRoutes ~> check {
        status mustBe StatusCodes.OK
        responseAs[Receipt].status mustBe IRSVerified
      }
    }
  }
}
