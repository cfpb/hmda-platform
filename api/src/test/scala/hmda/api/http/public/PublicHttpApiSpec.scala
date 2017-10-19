package hmda.api.http.public

import akka.event.{ LoggingAdapter, NoLogging }
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.util.Timeout
import hmda.api.RequestHeaderUtils
import hmda.api.model.public.ULIModel.{ Loan, ULI, ULICheck, ULIValidated }
import hmda.api.protocol.public.ULIProtocol
import hmda.model.fi.lar.LarGenerators
import hmda.query.repository.filing.LarConverter._
import org.scalatest.{ BeforeAndAfterAll, MustMatchers, WordSpec }
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._

import scala.concurrent.duration._

class PublicHttpApiSpec extends WordSpec with MustMatchers with BeforeAndAfterAll
    with ScalatestRouteTest with RequestHeaderUtils with PublicHttpApi with LarGenerators with ULIProtocol {

  override val log: LoggingAdapter = NoLogging
  implicit val ec = system.dispatcher

  val duration = 10.seconds
  override implicit val timeout = Timeout(duration)

  val p = "2017"
  val l1 = toLoanApplicationRegisterQuery(sampleLar).copy(period = p, institutionId = "0")
  val l2 = toLoanApplicationRegisterQuery(sampleLar).copy(period = p, institutionId = "0")

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
    val loanId = "10Bx939c5543TqA1144M999143X"
    val checkDigit = 38
    val uli = "10Bx939c5543TqA1144M999143X" + checkDigit
    val loan = Loan(loanId)
    val uliCheck = ULICheck(uli)

    "return check digit and ULI from loan id" in {
      Post("/uli/check-digit", loan) ~> publicHttpRoutes ~> check {
        responseAs[ULI] mustBe ULI(loanId, checkDigit, uli)
      }
    }
    "Validate ULI" in {
      Post("/uli/validate", uliCheck) ~> publicHttpRoutes ~> check {
        responseAs[ULIValidated] mustBe ULIValidated(true)
      }
    }
  }

}
