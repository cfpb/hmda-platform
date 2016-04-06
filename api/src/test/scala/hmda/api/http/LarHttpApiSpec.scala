package hmda.api.http

import akka.event.{ LoggingAdapter, NoLogging }
import akka.http.javadsl.model.StatusCodes
import akka.http.scaladsl.testkit.ScalatestRouteTest
import hmda.model.fi.lar.LoanApplicationRegister
import hmda.parser.fi.lar.LarCsvParser
import org.scalatest.{ MustMatchers, WordSpec }
import scala.concurrent.ExecutionContext
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._

class LarHttpApiSpec extends WordSpec with MustMatchers with ScalatestRouteTest with LarHttpApi {

  override val log: LoggingAdapter = NoLogging
  val ec: ExecutionContext = system.dispatcher

  val larCsv = "2|0123456789|9|ABCDEFGHIJKLMNOPQRSTUVWXY|20130117|4|3|2|1|10000|1|5|20130119|06920|06|034|0100.01|4|5|7|4|3|2|1|8|7|6|5|4|1|2|9000|0|9|8|7|01.05|2|4"
  val lar = LarCsvParser(larCsv)

  "LAR HTTP Service" must {
    "parse a valid pipe delimited LAR and return JSON representation" in {
      Post("/lar/parse", larCsv) ~> larRoutes ~> check {
        status mustEqual StatusCodes.OK
        responseAs[LoanApplicationRegister] mustBe lar
      }
    }
  }

}
