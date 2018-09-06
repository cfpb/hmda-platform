package hmda.api.http.public

import akka.event.{LoggingAdapter, NoLogging}
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.util.Timeout
import hmda.api.http.model.public.{
  SingleValidationErrorResult,
  TsValidateRequest,
  TsValidateResponse
}
import org.scalatest.{MustMatchers, WordSpec}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import hmda.model.filing.ts.{Address, Contact, TransmittalSheet}
import hmda.model.institution.Agency
import io.circe.generic.auto._
import hmda.api.http.codec.filing.TsCodec._

class TsValidationHttpApiSpec
    extends WordSpec
    with MustMatchers
    with ScalatestRouteTest
    with TsValidationHttpApi {
  override val log: LoggingAdapter = NoLogging
  val ec: ExecutionContext = system.dispatcher
  override implicit val timeout: Timeout = Timeout(5.seconds)

  val ts = TransmittalSheet(
    1,
    "Bank 0",
    2018,
    4,
    Contact("Jane Smith",
            "111-111-1111",
            "jane.smith@bank0.com",
            Address("1600 Pennsylvania Ave NW", "Washington", "DC", "20500")),
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
        responseAs[TsValidateResponse].errorMessages mustBe List(
          "id is not numeric",
          "agency code is not numeric")
      }
    }

    "fail to parse a valid pipe delimited TS with too many fields and return an error" in {
      val tsValidateRequestWithTooManyFields =
        TsValidateRequest(tsCsv + "|too|many|fields")
      Post("/ts/parse", tsValidateRequestWithTooManyFields) ~> tsRoutes ~> check {
        status mustBe StatusCodes.BadRequest
        responseAs[TsValidateResponse].errorMessages mustBe List(
          "An incorrect number of data fields were reported: 18 data fields were found, when 15 data fields were expected.")
      }
    }

    "return all errors when TS is invalid" in {
      Post("/ts/validate", TsValidateRequest(invalidCsv)) ~> tsRoutes ~> check {
        status mustBe StatusCodes.OK
        responseAs[SingleValidationErrorResult].syntactical.errors.size mustBe 1
        responseAs[SingleValidationErrorResult].validity.errors.size mustBe 1
      }
    }
    "filter syntactical TS errors" in {
      Post("/ts/validate?check=syntactical", TsValidateRequest(invalidCsv)) ~> tsRoutes ~> check {
        status mustBe StatusCodes.OK
        responseAs[SingleValidationErrorResult].syntactical.errors.size mustBe 1
        responseAs[SingleValidationErrorResult].validity.errors.size mustBe 0
      }
    }
    "filter validity TS errors" in {
      Post("/ts/validate?check=validity", TsValidateRequest(invalidCsv)) ~> tsRoutes ~> check {
        status mustBe StatusCodes.OK
        responseAs[SingleValidationErrorResult].syntactical.errors.size mustBe 0
        responseAs[SingleValidationErrorResult].validity.errors.size mustBe 1
      }
    }
  }
}
