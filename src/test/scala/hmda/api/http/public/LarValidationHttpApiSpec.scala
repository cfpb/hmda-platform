package hmda.api.http.public

import akka.event.{LoggingAdapter, NoLogging}
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.util.Timeout
import hmda.api.http.model.public.{
  LarValidateRequest,
  LarValidateResponse,
  ValidatedResponse
}
import org.scalatest.{MustMatchers, WordSpec}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.ts.TsGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe.generic.auto._
import hmda.api.http.codec.LarCodec._
import hmda.api.http.util.FileUploadUtils
import hmda.model.filing.ts.TransmittalSheet

class LarValidationHttpApiSpec
    extends WordSpec
    with MustMatchers
    with ScalatestRouteTest
    with LarValidationHttpApi
    with FileUploadUtils {
  override val log: LoggingAdapter = NoLogging
  val ec: ExecutionContext = system.dispatcher
  override implicit val timeout: Timeout = Timeout(5.seconds)

  val lar = larGen.sample.get
  val larCsv = lar.toCSV

  val lars = larNGen(10).sample.getOrElse(Nil)
  val ts = tsGen.sample.getOrElse(TransmittalSheet())

  "LAR HTTP Service" must {
    "parse a valid pipe delimited LAR and return JSON representation" in {
      val larValidateRequest = LarValidateRequest(larCsv)
      Post("/lar/parse", larValidateRequest) ~> larRoutes ~> check {
        response.status mustBe StatusCodes.OK
        responseAs[LoanApplicationRegister] mustBe lar
      }
    }

    "fail to parse an invalid pipe delimited LAR and return a list of errors" in {
      val csv = larGen.sample.get.toCSV
      val values = csv.split('|').map(_.trim)
      val badValues = values.head.replace("2", "A") ++ values.tail
      val badCsv = badValues.mkString("|")
      Post("/lar/parse", LarValidateRequest(badCsv)) ~> larRoutes ~> check {
        status mustBe StatusCodes.BadRequest
        responseAs[LarValidateResponse] mustBe LarValidateResponse(
          List("id is not numeric"))
      }
    }

    "fail to parse a valid pipe delimited LAR with too many fields and return an error" in {
      Post("/lar/parse", LarValidateRequest(larCsv + "|too|many|fields")) ~> larRoutes ~> check {
        status mustBe StatusCodes.BadRequest
        responseAs[LarValidateResponse] mustBe LarValidateResponse(List(
          "An incorrect number of data fields were reported: 113 data fields were found, when 110 data fields were expected."))
      }
    }

    "parse a file of LAR" in {
      val tsCsv = s"${ts.toCSV}\n"
      val larsCsv = lars.map(lar => s"${lar.toCSV}\n")
      val csv = tsCsv + larsCsv.mkString("")

      val file = multipartFile(csv, "auto-gen-lars.txt")

      Post("/lar/parse", file) ~> larRoutes ~> check {
        status mustBe StatusCodes.OK
        val result = responseAs[ValidatedResponse]
        result.validated.size mustBe 0
      }

    }

  }

}
