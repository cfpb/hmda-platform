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
import akka.util.Timeout
import hmda.api.RequestHeaderUtils
import hmda.api.model.ErrorResponse
import hmda.api.processing.lar.SingleLarValidation
import hmda.validation.engine.ValidationError
import spray.json._

class LarHttpApiSpec extends WordSpec with MustMatchers with ScalatestRouteTest
    with LarHttpApi with RequestHeaderUtils {

  override val log: LoggingAdapter = NoLogging
  override implicit val timeout: Timeout = Timeout(5.seconds)

  val ec: ExecutionContext = system.dispatcher

  implicit val routeTestTimeout: RouteTestTimeout = RouteTestTimeout(5.seconds)

  //Start up API Actors
  val larValidation = system.actorOf(SingleLarValidation.props, "larValidation")

  val larCsv = "2|0123456789|9|ABCDEFGHIJKLMNOPQRSTUVWXY|NA|4|2|2|1|100|3|6|20130119|14454|25|025|0001.00|4|3|5|4|3|2|1|6|||||1|2|NA|0||||NA|2|4"
  val invalidLarCsv = "invalid|0123456789|invalid|ABCDEFGHIJKLMNOPQRSTUVWXY|NA|4|2|2|1|100|3|6|20130119|14454|25|025|0001.00|4|3|5|4|3|2|1|6|||||1|2|NA|0||||NA|2|4"

  val lar = LarCsvParser(larCsv).right.get // Assuming the hardcoded value will parse correctly
  val larJson = lar.toJson

  "LAR HTTP Service" must {
    "parse a valid pipe delimited LAR and return JSON representation" in {
      postWithCfpbHeaders("/lar/parse", larCsv) ~> larRoutes ~> check {
        status mustEqual StatusCodes.OK
        responseAs[LoanApplicationRegister] mustBe lar
      }
    }

    "fail to parse an invalid pipe delimited LAR and return a list of errors" in {
      postWithCfpbHeaders("/lar/parse", invalidLarCsv) ~> larRoutes ~> check {
        status mustEqual StatusCodes.BAD_REQUEST
        responseAs[List[String]].length mustBe 2
      }
    }

    "fail to parse an valid pipe delimited LAR with too many fields and return an error" in {
      postWithCfpbHeaders("/lar/parse", larCsv + "|too|many|fields") ~> larRoutes ~> check {
        status mustEqual StatusCodes.BAD_REQUEST
        responseAs[List[String]].length mustBe 1
      }
    }

    "return no validation errors for a valid LAR" in {
      postWithCfpbHeaders("/lar/validate", lar) ~> larRoutes ~> check {
        status mustEqual StatusCodes.OK
        responseAs[List[ValidationError]] mustBe Nil
      }
    }

    "return validation error for invalid LAR (S020, agency code not in valid values domain)" in {
      val badLar = lar.copy(agencyCode = 0)
      postWithCfpbHeaders("/lar/validate", badLar) ~> larRoutes ~> check {
        status mustEqual StatusCodes.OK
        responseAs[List[ValidationError]].length mustBe 1
      }
    }

    "filter syntactical, validity, or quality only for invalid LAR with all 3 kinds of errors" in {
      val badLoanType = lar.loan.copy(loanType = 0, amount = 900, propertyType = 2)
      val badLar = lar.copy(agencyCode = 0, loan = badLoanType, purchaserType = 4)
      postWithCfpbHeaders("/lar/validate", badLar) ~> larRoutes ~> check {
        status mustEqual StatusCodes.OK
        responseAs[List[ValidationError]].length mustBe 3
      }
      //should fail S020
      postWithCfpbHeaders("/lar/validate?check=syntactical", badLar) ~> larRoutes ~> check {
        status mustEqual StatusCodes.OK
        responseAs[List[ValidationError]].length mustBe 1
      }
      //should fail V220
      postWithCfpbHeaders("/lar/validate?check=validity", badLar) ~> larRoutes ~> check {
        status mustEqual StatusCodes.OK
        responseAs[List[ValidationError]].length mustBe 1
      }
      //should fail Q036
      postWithCfpbHeaders("/lar/validate?check=quality", badLar) ~> larRoutes ~> check {
        status mustEqual StatusCodes.OK
        responseAs[List[ValidationError]].length mustBe 1
      }
    }
  }

  /*
  "reject requests without 'CFPB-HMDA-Username' header" in {
    // Request the endpoint without username header (but with other headers)
    Post("/lar/parse", larCsv).addHeader(institutionsHeader) ~> larRoutes ~> check {
      status mustBe StatusCodes.FORBIDDEN
      responseAs[ErrorResponse] mustBe ErrorResponse(403, "Unauthorized Access", "")
    }
  }

  "reject requests without 'CFPB-HMDA-Institutions' header" in {
    // Request the endpoint without institutions header (but with other headers)
    Post("/lar/parse", larCsv).addHeader(usernameHeader) ~> larRoutes ~> check {
      status mustBe StatusCodes.FORBIDDEN
      responseAs[ErrorResponse] mustBe ErrorResponse(403, "Unauthorized Access", "")
    }
  }
  */

}
