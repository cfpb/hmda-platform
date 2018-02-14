package hmda.api.http.public

import akka.event.{ LoggingAdapter, NoLogging }
import akka.http.scaladsl.model.headers.{ HttpEncodings, `Accept-Encoding` }
import akka.http.scaladsl.model.headers.HttpEncodings.deflate
import akka.http.scaladsl.testkit.{ RouteTestTimeout, ScalatestRouteTest }
import akka.util.Timeout
import hmda.api.RequestHeaderUtils
import hmda.parser.fi.ts.TsCsvParser
import hmda.persistence.HmdaSupervisor
import hmda.validation.stats.ValidationStats
import org.scalatest.{ MustMatchers, WordSpec }
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.StatusCodes
import hmda.api.model.SingleValidationErrorResult
import hmda.model.fi.ts.{ Parent, Respondent, TransmittalSheet }
import hmda.validation.engine.ValidationErrorsSummary
import spray.json._

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

class SingleTsValidationHttpApiSpec extends WordSpec with ScalatestRouteTest with SingleTsValidationHttpApi
    with RequestHeaderUtils with MustMatchers {
  override val log: LoggingAdapter = NoLogging
  val ec: ExecutionContext = system.dispatcher
  override implicit val timeout: Timeout = Timeout(5.seconds)

  implicit val routeTestTimeout: RouteTestTimeout = RouteTestTimeout(5.seconds)

  //Start up API Actors
  val validationStats = ValidationStats.createValidationStats(system)
  val supervisor = HmdaSupervisor.createSupervisor(system, validationStats)

  val tsCsv = "1|0123456789|9|201301171330|2013|12-9379899|900|MIKES SMALL BANK   XXXXXXXXXXX|1234 Main St       XXXXXXXXXXXXXXXXXXXXX|Sacramento         XXXXXX|CA|99999-9999|MIKES SMALL INC    XXXXXXXXXXX|1234 Kearney St    XXXXXXXXXXXXXXXXXXXXX|San Francisco      XXXXXX|CA|99999-1234|Mrs. Krabappel     XXXXXXXXXXX|916-999-9999|999-753-9999|krabappel@gmail.comXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"
  val invalidTsCsv = "invalid|0123456789|9|201301171330|2013|99-9999999|900|MIKES SMALL BANK   XXXXXXXXXXX|1234 Main St       XXXXXXXXXXXXXXXXXXXXX|Sacramento         XXXXXX|CA|99999-9999|MIKES SMALL INC    XXXXXXXXXXX|1234 Kearney St    XXXXXXXXXXXXXXXXXXXXX|San Francisco      XXXXXX|CA|99999-1234|Mrs. Krabappel     XXXXXXXXXXX|916-999-9999|999-753-9999|krabappel@gmail.comXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"

  val ts = TsCsvParser(tsCsv).right.get
  val tsJson = ts.toJson

  "TS HTTP Service" must {
    "parse a valid pipe delimited TS and return JSON representation" in {
      postWithCfpbHeaders("/ts/parse", tsCsv) ~> tsRoutes(supervisor) ~> check {
        status mustEqual StatusCodes.OK
        responseAs[TransmittalSheet] mustBe ts
      }
    }

    "fail to parse an invalid pipe delimited TS and return list of errors" in {
      postWithCfpbHeaders("/ts/parse", invalidTsCsv) ~> tsRoutes(supervisor) ~> check {
        status mustEqual StatusCodes.BadRequest
        responseAs[List[String]] mustBe List("Record Identifier is not an integer")
      }
    }

    "fail to parse a valid pipe delinited TS with too many fields and return an error" in {
      postWithCfpbHeaders("/ts/parse", tsCsv + "|too|many|fields") ~> tsRoutes(supervisor) ~> check {
        status mustEqual StatusCodes.BadRequest
        responseAs[List[String]] mustBe List("An incorrect number of data fields were reported: 24 data fields were found, when 21 data fields were expected.")
      }
    }

    "return no validation errors for a valid TS" in {
      postWithCfpbHeaders("/ts/validate", ts) ~> tsRoutes(supervisor) ~> check {
        status mustEqual StatusCodes.OK
        responseAs[SingleValidationErrorResult] mustBe
          SingleValidationErrorResult(
            ValidationErrorsSummary(Nil),
            ValidationErrorsSummary(Nil),
            ValidationErrorsSummary(Nil)
          )
      }
    }

    "return validation error for invalid TS (S020, agency code not in valid values domain)" in {
      val invalidTs = ts.copy(agencyCode = 0)
      postWithCfpbHeaders("/ts/validate", invalidTs) ~> tsRoutes(supervisor) ~> check {
        status mustEqual StatusCodes.OK
        responseAs[SingleValidationErrorResult].syntactical.errors.length mustBe 1
      }
    }

    "filter syntactical, validity or quality only for invalid TS with all 3 kinds of errors" in {
      val addr = "Address"
      val resp = Respondent("id", "resp", address = addr, "city", "CA", "92010")
      val p = Parent("", address = addr, "", "", "")
      val badTs = ts.copy(agencyCode = 0, taxId = "00-0000000", respondent = resp, parent = p)

      postWithCfpbHeaders("/ts/validate", badTs) ~> tsRoutes(supervisor) ~> check {
        status mustEqual StatusCodes.OK
        responseAs[SingleValidationErrorResult].syntactical.errors.length mustBe 1
        responseAs[SingleValidationErrorResult].validity.errors.length mustBe 1
        responseAs[SingleValidationErrorResult].quality.errors.length mustBe 1

      }

      //Should fail S020
      postWithCfpbHeaders("/ts/validate?check=syntactical", badTs) ~> tsRoutes(supervisor) ~> check {
        status mustEqual StatusCodes.OK
        responseAs[SingleValidationErrorResult].syntactical.errors.length mustBe 1
      }

      //Should fail V125
      postWithCfpbHeaders("/ts/validate?check=validity", badTs) ~> tsRoutes(supervisor) ~> check {
        status mustEqual StatusCodes.OK
        responseAs[SingleValidationErrorResult].validity.errors.length mustBe 1
      }

      //Should fail Q020
      postWithCfpbHeaders("/ts/validate?check=quality", badTs) ~> tsRoutes(supervisor) ~> check {
        status mustEqual StatusCodes.OK
        responseAs[SingleValidationErrorResult].quality.errors.length mustBe 1
      }

    }

    "return no errors when parsing and validating a valid TS" in {
      postWithCfpbHeaders("/ts/parseAndValidate", tsCsv) ~> tsRoutes(supervisor) ~> check {
        status mustEqual StatusCodes.OK
        responseAs[SingleValidationErrorResult] mustBe
          SingleValidationErrorResult(
            ValidationErrorsSummary(Nil),
            ValidationErrorsSummary(Nil),
            ValidationErrorsSummary(Nil)
          )
      }
    }

  }

  "TS API Authorization and rejection handling" must {
    // These endpoints are not protected by authorization, so they should have
    // the same result whether or not the request contains auth headers.

    "allow requests without 'CFPB-HMDA-Institutions' header" in {
      Post("/ts/parse", tsCsv).addHeader(usernameHeader) ~> tsRoutes(supervisor) ~> check {
        status mustEqual StatusCodes.OK
        responseAs[TransmittalSheet] mustBe ts
      }
    }

    "allow requests without 'CFPB-HMDA-Username' header" in {
      Post("/ts/validate", ts).addHeader(institutionsHeader) ~> tsRoutes(supervisor) ~> check {
        status mustEqual StatusCodes.OK
        responseAs[SingleValidationErrorResult].syntactical.errors mustBe Nil
        responseAs[SingleValidationErrorResult].validity.errors mustBe Nil
        responseAs[SingleValidationErrorResult].quality.errors mustBe Nil
      }
    }

    "allow requests without 'CFPB-HMDA-Username' or 'CFPB-HMDA-Institutions' headers" in {
      Post("/ts/parseAndValidate", tsCsv) ~> tsRoutes(supervisor) ~> check {
        status mustEqual StatusCodes.OK
        responseAs[SingleValidationErrorResult].syntactical.errors mustBe Nil
        responseAs[SingleValidationErrorResult].validity.errors mustBe Nil
        responseAs[SingleValidationErrorResult].quality.errors mustBe Nil
      }
    }

    "not handle routes that aren't defined in this API" in {
      getWithCfpbHeaders("/institutions") ~> tsRoutes(supervisor) ~> check {
        handled mustBe false
        rejections mustBe List()
      }
    }

  }

  "Endpoint Response Encoding" must {
    "use requested encoding" in {
      Post("/ts/parse", tsCsv).addHeader(`Accept-Encoding`(deflate)) ~> tsRoutes(supervisor) ~> check {
        response.encoding mustBe HttpEncodings.deflate
      }
    }

    "use 'identity' when no encoding is requested" in {
      Post("/ts/validate", ts) ~> tsRoutes(supervisor) ~> check {
        response.encoding mustBe HttpEncodings.identity
      }
    }
  }

}
