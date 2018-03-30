package hmda.api.http.public

import akka.event.{LoggingAdapter, NoLogging}
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.util.Timeout
import hmda.api.http.model.public.TsValidateRequest
import org.scalatest.{MustMatchers, WordSpec}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import hmda.model.filing.ts.{Address, Contact, TransmittalSheet}
import hmda.model.institution.Agency
import io.circe.generic.auto._
import hmda.api.http.codec.TsCodec._

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
  val invalidCsv =
    "A|Bank 0|2018|4|Jane Smith|111-111-1111|jane.smith@bank0.com|1600 Pennsylvania Ave NW|Washington|DC|20500|A|100|99-999999|10Bx939c5543TqA1144M"

  val tsValidateRequest = TsValidateRequest(tsCsv)

  "TS HTTP Service" must {
    "parse a valid pipe delimited TS and return JSON representation" in {
      Post("/ts/parse", tsValidateRequest) ~> tsRoutes ~> check {
        status mustBe StatusCodes.OK
        responseAs[TransmittalSheet] mustBe ts
      }
    }

    "fail to parse an invalid pipe delimited TS and return list of errors" in {
      Post("/ts/parse", TsValidateRequest(invalidCsv)) ~> tsRoutes ~> check {
        status mustBe StatusCodes.BadRequest
        responseAs[List[String]] mustBe List("id is not numeric",
                                             "agency code is not numeric")
      }
    }

    "fail to parse a valid pipe delimited TS with too many fields and return an error" in {
      val tsValidateRequestWithTooManyFields =
        TsValidateRequest(tsCsv + "|too|many|fields")
      Post("/ts/parse", tsValidateRequestWithTooManyFields) ~> tsRoutes ~> check {
        status mustBe StatusCodes.BadRequest
        responseAs[List[String]] mustBe List(
          "An incorrect number of data fields were reported: 18 data fields were found, when 15 data fields were expected.")
      }
    }
  }
}
