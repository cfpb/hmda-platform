package hmda.api.http.public

import akka.event.{LoggingAdapter, NoLogging}
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.util.Timeout
import hmda.api.http.model.public.LarValidateRequest
import org.scalatest.{MustMatchers, WordSpec}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe.generic.auto._

class LarValidationHttpApiSpec extends WordSpec with MustMatchers with ScalatestRouteTest with LarValidationHttpApi  {
  override val log: LoggingAdapter = NoLogging
  val ec: ExecutionContext = system.dispatcher
  override implicit val timeout: Timeout = Timeout(5.seconds)



  val lar = larGen.sample.get
  val larCsv = lar.toCSV


  "LAR HTTP Service" must {
    "parse a valid pipe delimited TS and return JSON representation" in {
      val larValidateRequest = LarValidateRequest(larCsv)
      Post("/lar/parse", larValidateRequest) ~> larRoutes ~> check {
        response.status mustBe StatusCodes.OK
        responseAs[LoanApplicationRegister] mustBe lar
      }
    }
  }


}
