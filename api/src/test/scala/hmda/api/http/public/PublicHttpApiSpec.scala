package hmda.api.http.public

import akka.event.{ LoggingAdapter, NoLogging }
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.util.Timeout
import hmda.api.RequestHeaderUtils
import hmda.api.model.public.ULIModel._
import hmda.api.protocol.public.ULIProtocol
import hmda.model.fi.lar.LarGenerators
import org.scalatest.{ BeforeAndAfterAll, MustMatchers, WordSpec }
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import hmda.api.http.FileUploadUtils

import scala.concurrent.duration._

class PublicHttpApiSpec extends WordSpec with MustMatchers with BeforeAndAfterAll
    with ScalatestRouteTest with RequestHeaderUtils with PublicHttpApi with FileUploadUtils with LarGenerators with ULIProtocol {

  override val log: LoggingAdapter = NoLogging
  implicit val ec = system.dispatcher

  val duration = 10.seconds
  override implicit val timeout = Timeout(duration)

  val p = "2017"

  val uliTxt = "10Cx939c5543TqA1144M999143X10\n" +
    "10Bx939c5543TqA1144M999143X38\n" +
    "10Bx939c5543TqA1144M999133X38\n"

  val loanTxt = "10Bx939c5543TqA1144M999143X\n" +
    "10Cx939c5543TqA1144M999143X"

  "Modified LAR Http API" must {
    "return list of modified LARs in proper format" in {
      Get("/institutions/0/filings/2017/lar") ~> publicHttpRoutes ~> check {
        status mustBe StatusCodes.OK
        //TODO: update when modified lar is reimplemented
        //contentType mustBe ContentTypes.`text/csv(UTF-8)`
        //responseAs[String].split("\n") must have size 2
      }
    }
  }

  "ULI API" must {
    val uliFile = multiPartFile(uliTxt, "ulis.txt")
    val loanFile = multiPartFile(loanTxt, "loanIds.txt")
    val loanId = "10Bx939c5543TqA1144M999143X"
    val checkDigit = 38
    val uli = "10Bx939c5543TqA1144M999143X" + checkDigit
    val loan = Loan(loanId)
    val uliCheck = ULICheck(uli)

    "return check digit and ULI from loan id" in {
      Post("/uli/checkDigit", loan) ~> publicHttpRoutes ~> check {
        responseAs[ULI] mustBe ULI(loanId, checkDigit, uli)
      }
    }
    "return check digit and ULI from file of loan ids" in {
      Post("/uli/checkDigit", loanFile) ~> publicHttpRoutes ~> check {
        status mustBe StatusCodes.OK
        responseAs[LoanCheckDigitResponse].loanIds mustBe Seq(
          ULI("10Bx939c5543TqA1144M999143X", 38, "10Bx939c5543TqA1144M999143X38"),
          ULI("10Cx939c5543TqA1144M999143X", 10, "10Cx939c5543TqA1144M999143X10")
        )
      }
    }
    "return csv with check digit and ULI from file of loan ids" in {
      Post("/uli/checkDigit/csv", loanFile) ~> publicHttpRoutes ~> check {
        status mustBe StatusCodes.OK
        val csv = responseAs[String]
        csv must include("loanId,checkDigit,uli")
        csv must include("10Bx939c5543TqA1144M999143X,38,10Bx939c5543TqA1144M999143X38")
        csv must include("10Cx939c5543TqA1144M999143X,10,10Cx939c5543TqA1144M999143X10")
      }
    }
    "Validate ULI" in {
      Post("/uli/validate", uliCheck) ~> publicHttpRoutes ~> check {
        responseAs[ULIValidated] mustBe ULIValidated(true)
      }
    }
    "validate a file of ULIs and return csv" in {
      Post("/uli/validate/csv", uliFile) ~> publicHttpRoutes ~> check {
        status mustBe StatusCodes.OK
        val csv = responseAs[String]
        csv must include("uli,isValid")
        csv must include("10Cx939c5543TqA1144M999143X10,true")
        csv must include("10Bx939c5543TqA1144M999143X38,true")
        csv must include("10Bx939c5543TqA1144M999133X38,false")
      }
    }
    "validate a file of ULIs" in {
      Post("/uli/validate", uliFile) ~> publicHttpRoutes ~> check {
        status mustBe StatusCodes.OK
        responseAs[ULIBatchValidatedResponse].ulis mustBe Seq(
          ULIBatchValidated("10Cx939c5543TqA1144M999143X10", true),
          ULIBatchValidated("10Bx939c5543TqA1144M999143X38", true),
          ULIBatchValidated("10Bx939c5543TqA1144M999133X38", false)
        )
      }
    }
  }

}
