package hmda.api.http

import akka.event.{ LoggingAdapter, NoLogging }
import akka.http.javadsl.model.StatusCodes
import akka.http.scaladsl.testkit.{ RouteTestTimeout, ScalatestRouteTest }
import hmda.model.fi.lar.LoanApplicationRegister
import hmda.parser.fi.lar.LarCsvParser
import org.scalatest.{ MustMatchers, WordSpec }
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import hmda.api.processing.lar.SingleLarValidation
import hmda.validation.engine.ValidationError
import spray.json._

class LarHttpApiSpec extends WordSpec with MustMatchers with ScalatestRouteTest with LarHttpApi {

  override val log: LoggingAdapter = NoLogging
  val ec: ExecutionContext = system.dispatcher

  implicit val routeTestTimeout: RouteTestTimeout = RouteTestTimeout(5.seconds)

  //Start up API Actors
  val larValidation = system.actorOf(SingleLarValidation.props, "larValidation")

  val larCsv = "2|8800009923|3|3944905973|20170827|1|2|1|1|75|3|1|20170915|34820|45|051|0801.01|2|2|5| | | | |5| | | | |2|1|109|0| | | |05.63|2|1"
  val lar = LarCsvParser(larCsv)
  val larJson = lar.toJson

  "LAR HTTP Service" must {
    "parse a valid pipe delimited LAR and return JSON representation" in {
      Post("/lar/parse", larCsv) ~> larRoutes ~> check {
        status mustEqual StatusCodes.OK
        responseAs[LoanApplicationRegister] mustBe lar
      }
    }

    "return no validation errors for a valid LAR" in {
      Post("/lar/validate", lar) ~> larRoutes ~> check {
        status mustEqual StatusCodes.OK
        responseAs[List[ValidationError]] mustBe Nil
      }
    }

    "return validation error for invalid LAR (S020, agency code not in valid values domain)" in {
      val badLar = lar.copy(agencyCode = 0)
      Post("/lar/validate", badLar) ~> larRoutes ~> check {
        status mustEqual StatusCodes.OK
        responseAs[List[ValidationError]].length mustBe 1
      }
    }

    "filter syntactical or validity only for invalid LAR with both syntactical and validity errors" in {
      val badLoanType = lar.loan.copy(loanType = 0)
      val badLar = lar.copy(agencyCode = 0, loan = badLoanType)
      Post("/lar/validate", badLar) ~> larRoutes ~> check {
        status mustEqual StatusCodes.OK
        responseAs[List[ValidationError]].length mustBe 2
      }
      Post("/lar/validate?check=syntactical", badLar) ~> larRoutes ~> check {
        status mustEqual StatusCodes.OK
        responseAs[List[ValidationError]].length mustBe 1
      }
      Post("/lar/validate?check=validity", badLar) ~> larRoutes ~> check {
        status mustEqual StatusCodes.OK
        responseAs[List[ValidationError]].length mustBe 1
      }
    }
  }

}
