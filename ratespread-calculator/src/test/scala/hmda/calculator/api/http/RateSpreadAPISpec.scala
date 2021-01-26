package hmda.uli.api.http

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.http.scaladsl.unmarshalling.Unmarshaller._
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import hmda.calculator.api.RateSpreadResponse
import hmda.calculator.api.http.RateSpreadAPIRoutes
import hmda.calculator.api.model.RateSpreadRequest
import hmda.calculator.apor.AporListEntity.AporOperation
import hmda.calculator.apor.{APOR, FixedRate, VariableRate}
import hmda.util.http.FileUploadUtils
import io.circe.generic.auto._
import org.scalatest.{BeforeAndAfterAll, MustMatchers, WordSpec}
import org.slf4j.{Logger, LoggerFactory}

import scala.concurrent.duration._

class RateSpreadAPISpec extends WordSpec with MustMatchers with BeforeAndAfterAll with ScalatestRouteTest with FileUploadUtils {

  val log: Logger = LoggerFactory.getLogger(getClass)
  implicit val ec = system.dispatcher

  val config = ConfigFactory.load()

  val duration         = 10.seconds
  implicit val timeout = Timeout(duration)
  val rateSpreadRoutes = RateSpreadAPIRoutes.create(log)

  val date = LocalDate.parse("2018-03-22", DateTimeFormatter.ISO_LOCAL_DATE)
  val singleRateSpread = RateSpreadRequest(
    actionTakenType = 1,
    loanTerm = 1,
    amortizationType = "FixedRate",
    apr = 3.0,
    lockInDate = date,
    reverseMortgage = 2
  )

  val rateSpreadTxt =
    "1,30,FixedRate,6.0,2018-03-22,2,2.010\n" +
      "1,30,VariableRate,6.0,2018-03-22,2,2.150\n"

  val rateSpreadFile = multipartFile(rateSpreadTxt, "rateSpread.txt")

  { // initialize global state
    AporOperation(APOR(
      LocalDate.parse("2018-03-22", DateTimeFormatter.ISO_LOCAL_DATE),
      Seq(1.01, 1.02, 1.03, 1.04, 1.05, 1.06, 1.07, 1.08, 1.09, 1.1, 1.11, 1.12, 1.13, 1.14, 1.15, 1.16, 1.17, 1.18, 1.19, 1.2, 1.21, 1.22,
        1.23, 1.24, 1.25, 1.26, 1.27, 1.28, 1.29, 1.3, 1.31, 1.32, 1.33, 1.34, 1.35, 1.36, 1.37, 1.38, 1.39, 1.40, 1.41, 1.42, 1.43, 1.44,
        1.45, 1.46, 1.47, 1.48, 1.49, 1.5)
    ), FixedRate)
    AporOperation(
      APOR(
        LocalDate.parse("2018-03-22", DateTimeFormatter.ISO_LOCAL_DATE),
        Seq(2.01, 2.02, 2.03, 2.04, 2.05, 2.06, 2.07, 2.08, 2.09, 2.1, 2.11, 2.12, 2.13, 2.14, 2.15, 2.16, 2.17, 2.18, 2.19, 2.2, 2.21, 2.22,
          2.23, 2.24, 2.25, 2.26, 2.27, 2.28, 2.29, 2.3, 2.31, 2.32, 2.33, 2.34, 2.35, 2.36, 2.37, 2.38, 2.39, 2.40, 2.41, 2.42, 2.43, 2.44,
          2.45, 2.46, 2.47, 2.48, 2.49, 2.5)
      ),
      VariableRate
    )
  }

  "Rate Spread API" must {
    "return single ratespread" in {
      Post("/rateSpread", singleRateSpread) ~> rateSpreadRoutes ~> check {
        responseAs[RateSpreadResponse] mustBe RateSpreadResponse("1.990")
      }
    }
    "return ratespread from csv" in {
      Post("/rateSpread/csv", rateSpreadFile) ~> rateSpreadRoutes ~> check {
        status mustBe StatusCodes.OK
        val csv = responseAs[String]
        csv must include("1,30,FixedRate,6.0,2018-03-22,2,2.010,4.700")
        csv must include("1,30,VariableRate,6.0,2018-03-22,2,2.150,3.700")
      }
    }

  }

}