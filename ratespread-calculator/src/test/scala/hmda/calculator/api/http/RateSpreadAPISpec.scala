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
import hmda.util.http.FileUploadUtils
import io.circe.generic.auto._
import org.scalatest.{ BeforeAndAfterAll, MustMatchers, WordSpec }
import org.slf4j.{ Logger, LoggerFactory }

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