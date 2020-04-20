package hmda.api.http.public

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.util.Timeout
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import hmda.api.http.model.filing.submissions.HmdaRowParsedErrorSummary
import hmda.api.http.model.public.{ SingleValidationErrorResult, TsValidateRequest }
import hmda.model.filing.ts.{ Address, Contact, TransmittalSheet }
import hmda.model.institution.Agency
import io.circe.generic.auto._
import org.scalatest.{ MustMatchers, WordSpec }
import org.slf4j.{ Logger, LoggerFactory }

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

class TsValidationHttpApiSpec extends WordSpec with MustMatchers with ScalatestRouteTest {
  val log: Logger               = LoggerFactory.getLogger(getClass)
  val ec: ExecutionContext      = system.dispatcher
  implicit val timeout: Timeout = Timeout(5.seconds)
  val tsRoutes                  = TsValidationHttpApi.create

  val ts = TransmittalSheet(
    1,
    "Bank 0",
    2018,
    4,
    Contact("Jane Smith", "111-111-1111", "jane.smith@bank0.com", Address("1600 Pennsylvania Ave NW", "Washington", "DC", "20500")),
    Agency.valueOf(9),
    100,
    "99-999999",
    "10Bx939c5543TqA1144M"
  )

  val tsCsv = ts.toCSV
  val invalidParseCsv =
    "A|Bank 0|2018|4|Jane Smith|111-111-1111|jane.smith@bank0.com|1600 Pennsylvania Ave NW|Washington|DC|20500|A|100|99-999999|10Bx939c5543TqA1144M"

  val invalidCsv =
    "0|Bank 0|2018|4|Jane|111-111-1111|janesmith@bank.com|123 Main St|Washington|DC|20001|9|100|99-999999|10Bx939c5543TqA1144M"

  val invalidCsv_2019 =
    "0|Bank 0|2019|4|Jane|111-111-1111|janesmith@bank.com|123 Main St|Washington|DC|20001|9|100|99-999999|10Bx939c5543TqA1144M"

  "TS HTTP Service" must {
    "return OPTIONS" in {
      Options("/ts/parse") ~> tsRoutes ~> check {
        status mustBe StatusCodes.OK
      }
    }
    "parse a valid pipe delimited TS and return JSON representation" in {
      val tsValidateRequest = TsValidateRequest(tsCsv)
      Post("/ts/parse", tsValidateRequest) ~> tsRoutes ~> check {
        status mustBe StatusCodes.OK
        responseAs[TransmittalSheet] mustBe ts
      }
    }

    "fail to parse an invalid pipe delimited TS and return list of errors" in {
      Post("/ts/parse", TsValidateRequest(invalidParseCsv)) ~> tsRoutes ~> check {
        status mustBe StatusCodes.BadRequest
        responseAs[HmdaRowParsedErrorSummary].errorMessages.length mustBe 2
      }
    }

    "fail to parse a valid pipe delimited TS with too many fields and return an error" in {
      val tsValidateRequestWithTooManyFields =
        TsValidateRequest(tsCsv + "|too|many|fields")
      Post("/ts/parse", tsValidateRequestWithTooManyFields) ~> tsRoutes ~> check {
        status mustBe StatusCodes.BadRequest
        responseAs[HmdaRowParsedErrorSummary].errorMessages.length mustBe 1
      }
    }

    "return all errors when TS is invalid - 2018" in {
      Post("/ts/validate/2018", TsValidateRequest(invalidCsv)) ~> tsRoutes ~> check {
        status mustBe StatusCodes.OK
        responseAs[SingleValidationErrorResult].syntactical.errors.size mustBe 1
        responseAs[SingleValidationErrorResult].validity.errors.size mustBe 1
      }
    }

    "return all errors when TS is invalid - 2019" in {
      Post("/ts/validate/2019", TsValidateRequest(invalidCsv_2019)) ~> tsRoutes ~> check {
        status mustBe StatusCodes.OK
        responseAs[SingleValidationErrorResult].syntactical.errors.size mustBe 1
        responseAs[SingleValidationErrorResult].validity.errors.size mustBe 1
      }
    }

    "filter syntactical TS errors" in {
      Post("/ts/validate/2018?check=syntactical", TsValidateRequest(invalidCsv)) ~> tsRoutes ~> check {
        status mustBe StatusCodes.OK
        responseAs[SingleValidationErrorResult].syntactical.errors.size mustBe 1
        responseAs[SingleValidationErrorResult].validity.errors.size mustBe 0
      }
    }
    "filter validity TS errors" in {
      Post("/ts/validate/2019?check=validity", TsValidateRequest(invalidCsv)) ~> tsRoutes ~> check {
        status mustBe StatusCodes.OK
        responseAs[SingleValidationErrorResult].syntactical.errors.size mustBe 0
        responseAs[SingleValidationErrorResult].validity.errors.size mustBe 1
      }
    }
  }
}