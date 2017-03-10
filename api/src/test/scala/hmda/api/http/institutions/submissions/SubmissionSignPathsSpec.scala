package hmda.api.http.institutions.submissions

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.Uri.Path
import hmda.api.http.{ InstitutionHttpApiAsyncSpec, InstitutionHttpApiSpec }
import hmda.api.model.{ ErrorResponse, Receipt }
import hmda.model.fi._
import spray.json.{ JsBoolean, JsObject }

class SubmissionSignPathsSpec extends InstitutionHttpApiAsyncSpec {
  val supervisor = system.actorSelection("/user/supervisor")

  val csv = "1|0123456789|9|201301171330|2013|99-9999999|900|MIKES SMALL BANK   XXXXXXXXXXX|1234 Main St       XXXXXXXXXXXXXXXXXXXXX|Sacramento         XXXXXX|CA|99999-9999|MIKES SMALL INC    XXXXXXXXXXX|1234 Kearney St    XXXXXXXXXXXXXXXXXXXXX|San Francisco      XXXXXX|CA|99999-1234|Mrs. Krabappel     XXXXXXXXXXX|916-999-9999|999-753-9999|krabappel@gmail.comXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX\n" +
    "2|0123456789|99|ABCDEFGHIJKLMNOPQRSTUVWXY|20130117|4|3|2|1|10000|1|5|20130119|06920|06|034|0100.01|4|5|7|4|3|2|1|8|7|6|5|4|1|2|9000|0|9|8|7|01.05|2|4\n" +
    "2|0123456789|9|ABCDEFGHIJKLMNOPQRSTUVWXY|20130117|4|3|2|1|10000|1|5|20130119|06920|06|034|0100.01|4|5|7|4|3|2|1|8|7|6|5|4|1|2|9000|0|9|8|7|01.05|2|4\n" +
    "2|0123456789|9|ABCDEFGHIJKLMNOPQRSTUVWXY|20130117|4|3|2|1|10000|1|5|20130119|06920|06|034|0100.01|4|5|7|4|3|2|1|8|7|6|5|4|1|2|9000|0|9|8|7|01.05|2|4"
  val file = multiPartFile(csv, "sample.txt")

  def signJson(v: Boolean): JsObject = JsObject("signed" -> JsBoolean(v))

  "Submission Sign Paths" must {
    "POST: Return 400 (Bad Request) when attempting to sign a submission that's not ready to sign" in {
      postWithCfpbHeaders("/institutions/0/filings/2017/submissions/1/sign", signJson(true)) ~> institutionsRoutes ~> check {
        status mustBe StatusCodes.BadRequest
        val err = responseAs[ErrorResponse]
        err.httpStatus mustBe 400
        err.path mustBe Path("/institutions/0/filings/2017/submissions/1/sign")
      }
    }

    /*
    "set up: get a submission to 'validated with errors' state" in {
    }
    */
    "GET: return an empty receipt when submission hasn't been signed" in {
      getWithCfpbHeaders("/institutions/0/filings/2017/submissions/1/sign") ~> institutionsRoutes ~> check {
        status mustBe StatusCodes.OK
        responseAs[Receipt] mustBe Receipt(0L, "", Created)
      }
    }
    "POST: Return 400 (Bad Request) when payload contains signed = false" in {
      postWithCfpbHeaders("/institutions/0/filings/2017/submissions/1/sign", signJson(false)) ~> institutionsRoutes ~> check {
        status mustBe StatusCodes.BadRequest
        val err = responseAs[ErrorResponse]
        err.httpStatus mustBe 400
        err.path mustBe Path("/institutions/0/filings/2017/submissions/1/sign")
      }
    }
    /*
    "POST: return filled receipt when payload contains signed = true" in {
      val signed = JsObject("signed" -> JsBoolean(true))

      postWithCfpbHeaders("/institutions/0/filings/2017/submissions/3/sign", signed) ~> institutionsRoutes ~> check {
        status mustBe StatusCodes.OK
        responseAs[Receipt].timestamp mustBe 5
        responseAs[Receipt].receipt mustBe "something real"
        responseAs[Receipt].status mustBe Signed
      }
    }

    "GET: return filled receipt when submissionStatus is signed" in {
      getWithCfpbHeaders("/institutions/0/filings/2017/submissions/3/sign") ~> institutionsRoutes ~> check {
        status mustBe StatusCodes.OK
        val receipt = responseAs[Receipt]
        receipt.status mustBe Signed
        receipt.timestamp mustBe 55
        receipt.receipt mustBe "same thing"
      }
    }
    */
  }
}
