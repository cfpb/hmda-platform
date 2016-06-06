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

  val larCsv = "2|0123456789|9|ABCDEFGHIJKLMNOPQRSTUVWXY|NA|4|2|2|1|100|3|6|20130119|14454|25|025|0001.00|4|3|5|4|3|2|1|6|||||1|2|NA|0||||NA|2|4"

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

    "filter syntactical, validity, or quality only for invalid LAR with all 3 kinds of errors" in {
      val badLoanType = lar.loan.copy(loanType = 0, amount = 900, propertyType = 2)
      val badLar = lar.copy(agencyCode = 0, loan = badLoanType, purchaserType = 4)
      Post("/lar/validate", badLar) ~> larRoutes ~> check {
        status mustEqual StatusCodes.OK
        responseAs[List[ValidationError]].length mustBe 3
      }
      //should fail S020
      Post("/lar/validate?check=syntactical", badLar) ~> larRoutes ~> check {
        status mustEqual StatusCodes.OK
        responseAs[List[ValidationError]].length mustBe 1
      }
      //should fail V220
      Post("/lar/validate?check=validity", badLar) ~> larRoutes ~> check {
        status mustEqual StatusCodes.OK
        responseAs[List[ValidationError]].length mustBe 1
      }
      //should fail Q036
      Post("/lar/validate?check=quality", badLar) ~> larRoutes ~> check {
        status mustEqual StatusCodes.OK
        responseAs[List[ValidationError]].length mustBe 1
      }
    }
  }

}
