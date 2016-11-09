package hmda.api.http

import akka.event.{ LoggingAdapter, NoLogging }
import akka.http.javadsl.model.StatusCodes
import akka.http.scaladsl.testkit.{ RouteTestTimeout, ScalatestRouteTest }
import hmda.api.model.SingleValidationErrorResult
import hmda.model.fi.lar.LoanApplicationRegister
import hmda.parser.fi.lar.LarCsvParser
import hmda.validation.engine.ValidationErrorsSummary
import org.scalatest.{ MustMatchers, WordSpec }
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.headers.HttpEncodings._
import akka.http.scaladsl.model.headers.HttpEncodings
import akka.http.scaladsl.model.headers.`Accept-Encoding`
import akka.util.Timeout
import hmda.api.RequestHeaderUtils
import hmda.persistence.HmdaSupervisor
import spray.json._

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

class LarHttpApiSpec extends WordSpec with MustMatchers with ScalatestRouteTest
    with LarHttpApi with RequestHeaderUtils {

  override val log: LoggingAdapter = NoLogging
  override implicit val timeout: Timeout = Timeout(5.seconds)

  val ec: ExecutionContext = system.dispatcher

  implicit val routeTestTimeout: RouteTestTimeout = RouteTestTimeout(5.seconds)

  //Start up API Actors
  val supervisor = HmdaSupervisor.createSupervisor(system)

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
        responseAs[SingleValidationErrorResult] mustBe
          SingleValidationErrorResult(
            ValidationErrorsSummary(Nil),
            ValidationErrorsSummary(Nil),
            ValidationErrorsSummary(Nil)
          )
      }
    }

    "return validation error for invalid LAR (S020, agency code not in valid values domain)" in {
      val badLar = lar.copy(agencyCode = 0)
      postWithCfpbHeaders("/lar/validate", badLar) ~> larRoutes ~> check {
        status mustEqual StatusCodes.OK
        responseAs[SingleValidationErrorResult].syntactical.errors.length mustBe 1
      }
    }

    "filter syntactical, validity, or quality only for invalid LAR with all 3 kinds of errors" in {
      val badLoanType = lar.loan.copy(loanType = 0, amount = 900, propertyType = 2)
      val badLar = lar.copy(agencyCode = 0, loan = badLoanType, purchaserType = 4)
      postWithCfpbHeaders("/lar/validate", badLar) ~> larRoutes ~> check {
        status mustEqual StatusCodes.OK
        responseAs[SingleValidationErrorResult].syntactical.errors.length mustBe 1
        responseAs[SingleValidationErrorResult].validity.errors.length mustBe 1
        responseAs[SingleValidationErrorResult].quality.errors.length mustBe 1

      }
      //should fail S020
      postWithCfpbHeaders("/lar/validate?check=syntactical", badLar) ~> larRoutes ~> check {
        status mustEqual StatusCodes.OK
        responseAs[SingleValidationErrorResult].syntactical.errors.length mustBe 1
      }
      //should fail V220
      postWithCfpbHeaders("/lar/validate?check=validity", badLar) ~> larRoutes ~> check {
        status mustEqual StatusCodes.OK
        responseAs[SingleValidationErrorResult].validity.errors.length mustBe 1
      }
      //should fail Q036
      postWithCfpbHeaders("/lar/validate?check=quality", badLar) ~> larRoutes ~> check {
        status mustEqual StatusCodes.OK
        responseAs[SingleValidationErrorResult].quality.errors.length mustBe 1
      }
    }

    "return no errors when parsing and validating a valid LAR" in {
      postWithCfpbHeaders("/lar/parseAndValidate", larCsv) ~> larRoutes ~> check {
        status mustEqual StatusCodes.OK
        responseAs[SingleValidationErrorResult] mustBe
          SingleValidationErrorResult(
            ValidationErrorsSummary(Nil),
            ValidationErrorsSummary(Nil),
            ValidationErrorsSummary(Nil)
          )
      }
    }

    "return parsing errors for an invalid LAR" in {
      postWithCfpbHeaders("/lar/parseAndValidate", invalidLarCsv) ~> larRoutes ~> check {
        status mustEqual StatusCodes.BAD_REQUEST
        responseAs[List[String]].length mustBe 2
      }
    }

    "return a list of validation errors for an invalid LAR" in {
      val badLar = lar.copy(agencyCode = 0)
      postWithCfpbHeaders("/lar/parseAndValidate", badLar.toCSV) ~> larRoutes ~> check {
        status mustEqual StatusCodes.OK
        responseAs[SingleValidationErrorResult].syntactical.errors.length mustBe 1
      }
    }
  }

  "LAR API Authorization and rejection handling" must {
    // These endpoints are not protected by authorization, so they should have
    // the same result whether or not the request contains auth headers.

    "allow requests without 'CFPB-HMDA-Institutions' header" in {
      Post("/lar/parse", larCsv).addHeader(usernameHeader) ~> larRoutes ~> check {
        status mustEqual StatusCodes.OK
        responseAs[LoanApplicationRegister] mustBe lar
      }
    }

    "allow requests without 'CFPB-HMDA-Username' header" in {
      Post("/lar/validate", lar).addHeader(institutionsHeader) ~> larRoutes ~> check {
        status mustEqual StatusCodes.OK
        responseAs[SingleValidationErrorResult].syntactical.errors mustBe Nil
        responseAs[SingleValidationErrorResult].validity.errors mustBe Nil
        responseAs[SingleValidationErrorResult].quality.errors mustBe Nil
      }
    }

    "not handle routes that aren't defined in this API" in {
      getWithCfpbHeaders("/institutions") ~> larRoutes ~> check {
        handled mustBe false
        rejections mustBe List()
      }
    }

  }

  "Endpoint Response Encoding" must {
    "use requested encoding" in {
      Post("/lar/parse", larCsv).addHeader(`Accept-Encoding`(deflate)) ~> larRoutes ~> check {
        response.encoding mustBe HttpEncodings.deflate
      }
    }

    "use 'identity' when no encoding is requested" in {
      Post("/lar/validate", lar) ~> larRoutes ~> check {
        response.encoding mustBe HttpEncodings.identity
      }
    }
  }

}
