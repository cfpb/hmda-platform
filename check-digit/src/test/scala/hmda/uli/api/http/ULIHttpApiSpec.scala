package hmda.uli.api.http

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.util.Timeout
import org.scalatest.{BeforeAndAfterAll, MustMatchers, WordSpec}
import akka.http.scaladsl.model.Uri.Path
import akka.http.scaladsl.model.headers.{HttpOrigin, Origin}
import com.typesafe.config.ConfigFactory
import hmda.api.http.model.ErrorResponse
import hmda.uli.api.model.ULIModel.{ULIBatchValidated, _}
import hmda.util.http.FileUploadUtils
import hmda.uli.api.model.ULIValidationErrorMessages._
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe.generic.auto._
import akka.http.scaladsl.unmarshalling.Unmarshaller._
import org.slf4j.{Logger, LoggerFactory}

import scala.concurrent.duration._

class ULIHttpApiSpec extends WordSpec with MustMatchers with BeforeAndAfterAll with ScalatestRouteTest with FileUploadUtils {

  val log: Logger   = LoggerFactory.getLogger(getClass)
  implicit val ec   = system.dispatcher
  val uliHttpRoutes = ULIHttpApi.create(log)

  val config = ConfigFactory.load()

  val duration         = 10.seconds
  implicit val timeout = Timeout(duration)

  val p = "2017"

  val uliTxt = "10Cx939c5543TqA1144M999143X10\n" +
    "10Bx939c5543TqA1144M999143X38\n" +
    "10Bx939c5543TqA1144M999133X38\n" +
    "#%)WQD!"

  val loanTxt = "10Bx939c5543TqA1144M999143X\n" +
    "10Cx939c5543TqA1144M999143X\n" +
    "##!d23e("

  "ULI API" must {
    val uliFile               = multipartFile(uliTxt, "ulis.txt")
    val loanFile              = multipartFile(loanTxt, "loanIds.txt")
    val loanId                = "10Bx939c5543TqA1144M999143X"
    val nonAlphanumericLoanId = "10Bx9#9c5543TqA1144M9@9143X"
    val checkDigit            = "38"
    val uli                   = "10Bx939c5543TqA1144M999143X" + checkDigit
    val loan                  = Loan(loanId)
    val nonAlphanumericLoan   = Loan(nonAlphanumericLoanId)
    val uliCheck              = ULICheck(uli)
    val shortUliCheck         = ULICheck("10Bx939c5")
    val longUliCheck          = ULICheck("10Bx939c5543TqA1144M999143X1dq921CQEMWEW45p0qsDDASDAGS2912dqXS1dq921CQEMWEW45p0qsDDASDAGS2912dqXS")
    val nonAlphaNumericCheck  = ULICheck("10Bx939c5543TqA1144M9@9143X")
    val longLoanId            = "10Bx939c5543TqA1144M999143X10Bx939c5543TqA1144M999143X"
    val longLoan              = Loan(longLoanId)
    "return check digit and ULI from loan id" in {
      Post("/uli/checkDigit", loan) ~> uliHttpRoutes ~> check {
        responseAs[ULI] mustBe ULI(loanId, checkDigit, uli)
      }
    }
    "return error responses for malformed loan ids" in {
      Post("/uli/checkDigit", nonAlphanumericLoan) ~> uliHttpRoutes ~> check {
        status mustBe StatusCodes.BadRequest
        val response = responseAs[ErrorResponse]
        response.message mustBe nonAlpanumericLoanIdMessage
        response.path mustBe Path("/uli/checkDigit")
      }
      Post("/uli/checkDigit", longLoan) ~> uliHttpRoutes ~> check {
        status mustBe StatusCodes.BadRequest
        val response = responseAs[ErrorResponse]
        response.message mustBe invalidLoanIdLengthMessage
      }
    }
    "include leading 0 for check digits < 10" in {
      val loanId = "5493001YS08XHF42M0372005203"
      Post("/uli/checkDigit", Loan(loanId)) ~> uliHttpRoutes ~> check {
        responseAs[ULI] mustBe ULI(loanId, "07", loanId + "07")
      }
      val lId2 = "asdgfhkjasdgfhkasd000"
      Post("/uli/checkDigit", Loan(lId2)) ~> uliHttpRoutes ~> check {
        responseAs[ULI] mustBe ULI(lId2, "03", lId2 + "03")
      }
    }
    "return check digit and ULI from file of loan ids" in {
      Post("/uli/checkDigit", loanFile) ~> uliHttpRoutes ~> check {
        status mustBe StatusCodes.OK
        responseAs[LoanCheckDigitResponse].loanIds mustBe Seq(
          ULI("10Bx939c5543TqA1144M999143X", "38", "10Bx939c5543TqA1144M999143X38"),
          ULI("10Cx939c5543TqA1144M999143X", "10", "10Cx939c5543TqA1144M999143X10"),
          ULI("##!d23e(", "Error", "Loan ID is not between 21 and 43 characters long")
        )
      }
    }
    "return csv with check digit and ULI from file of loan ids" in {
      Post("/uli/checkDigit/csv", loanFile) ~> uliHttpRoutes ~> check {
        status mustBe StatusCodes.OK
        val csv = responseAs[String]
        csv must include("loanId,checkDigit,uli")
        csv must include("10Bx939c5543TqA1144M999143X,38,10Bx939c5543TqA1144M999143X38")
        csv must include("10Cx939c5543TqA1144M999143X,10,10Cx939c5543TqA1144M999143X10")
      }
    }
    "Validate ULI" in {
      Post("/uli/validate", uliCheck) ~> Origin(HttpOrigin("http://ffiec.cfpb.gov")) ~> uliHttpRoutes ~> check {
        responseAs[ULIValidated] mustBe ULIValidated(true)
      }
      Post("/uli/validate", shortUliCheck) ~> uliHttpRoutes ~> check {
        status mustBe StatusCodes.BadRequest
        val response = responseAs[ErrorResponse]
        response.httpStatus mustBe 400
        response.path mustBe Path("/uli/validate")
      }
      Post("/uli/validate", longUliCheck) ~> uliHttpRoutes ~> check {
        status mustBe StatusCodes.BadRequest
        val response = responseAs[ErrorResponse]
        response.httpStatus mustBe 400
        response.message mustBe invalidULILengthMessage
        response.path mustBe Path("/uli/validate")
      }
      Post("/uli/validate", nonAlphaNumericCheck) ~> uliHttpRoutes ~> check {
        status mustBe StatusCodes.BadRequest
        val response = responseAs[ErrorResponse]
        response.path mustBe Path("/uli/validate")
        response.message mustBe nonAlphanumericULIMessage
      }
    }
    "validate a file of ULIs and return csv" in {
      Post("/uli/validate/csv", uliFile) ~> uliHttpRoutes ~> check {
        status mustBe StatusCodes.OK
        val csv = responseAs[String]
        csv must include("uli,isValid")
        csv must include("10Cx939c5543TqA1144M999143X10,true")
        csv must include("10Bx939c5543TqA1144M999143X38,true")
        csv must include("10Bx939c5543TqA1144M999133X38,false")
      }
    }
    "validate a file of ULIs" in {
      Post("/uli/validate", uliFile) ~> uliHttpRoutes ~> check {
        status mustBe StatusCodes.OK
        responseAs[ULIBatchValidatedResponse].ulis mustBe Seq(
          ULIBatchValidated("10Cx939c5543TqA1144M999143X10", "true"),
          ULIBatchValidated("10Bx939c5543TqA1144M999143X38", "true"),
          ULIBatchValidated("10Bx939c5543TqA1144M999133X38", "false") ,
          ULIBatchValidated("#%)WQD!", "ULI is not alphanumeric")

        )
      }
    }
  }

}