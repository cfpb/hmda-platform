package hmda.api.http.public

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import hmda.api.http.model.filing.submissions.HmdaRowParsedErrorSummary
import hmda.api.http.model.public.{ LarValidateRequest, SingleValidationErrorResult, ValidationErrorSummary, ValidationSingleErrorSummary }
import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.ts.TransmittalSheet
import hmda.model.filing.ts.TsGenerators._
import hmda.parser.filing.lar.LarCsvParser
import hmda.util.http.FileUploadUtils
import io.circe.generic.auto._
import org.scalatest.{ MustMatchers, WordSpec }

class LarValidationHttpApiSpec extends WordSpec with MustMatchers with ScalatestRouteTest with FileUploadUtils {
  val larRoutes: Route = LarValidationHttpApi.create

  val lar    = larGen.sample.getOrElse(LoanApplicationRegister())
  val larCsv = lar.toCSV

  val lars =
    larNGen(10).sample.getOrElse(List.fill(10)(LoanApplicationRegister()))
  val ts = tsGen.sample.getOrElse(TransmittalSheet())

  val tsCsv   = s"${ts.toCSV}\n"
  val larsCsv = lars.map(lar => s"${lar.toCSV}\n")
  val badLar =
    "2|AYAKEFD53DRJIQYNCI0U|AYAKEFD53DRJIQYNCI0U63EPM1BV6JMCBLQKVMQ9UMN35|20181217|4|5|2|2|1|16041|2|20181217|1234 Hocus Potato Way|Tatertown|VA|84096|49035|49035111906|1|||||8GMCACAP36H23X5P4CY43EKEW9U99R4VGENRVJ26M7YPH9U7O9PCXLZAWN08ZEVNW5GMGT|14|11|13|1|12|IN376P|1|2|2|3|5|4|4|XI523E3TOC6AA3J7IWQYPHNA7XQF6VZHLC5YMDLSOANWCJLP69S2PRIWV1L4W|AZAQ11|Y2O3RJE1NNVPLQIKPOVK2QTCPX367O8I2XGGW2854INVZGIMPYBANX82JFNOD7NP1PU|6|||||YL0MF86ZDTHSY4IR9XTN0943NWGII7N74ZWBVEYDFIAZ|IQS0OJCR|1NINW4K9VEJPOLIX3H3Z430MTXXIQAKVFTVXSOU7ITFO905GR2L58J4IBDGO0I0KVPUDRO9O21IPZATYBE3MQI|1|3|2|3|1|2|36|59|NA|0|NA|3|2|392|581|8|ZUXWM29BLH3MY74D09ZTO16HVBXJBY5DQ800PHRA2TCX8J6EV0KYFDAA17E2DIHBDOP|7||10|||||NA|NA|NA|NA|NA|9.7|NA|NA|188|NA|21|1|1|1|1|15962|3|5|28|17|2|2|LMIS8LM|3||||1||1||||2||1|1|2"
  val csv    = tsCsv + larsCsv.mkString("")
  val badCsv = tsCsv + larsCsv.mkString("") + s"$badLar"

  val validLar =
    "2|95GVQQ61RS6CWQF0SZD9|95GVQQ61RS6CWQF0SZD9F4VRXNN1OCVXHP1JURF9ZJS92|20180914|1|1|2|1|1|101000|1|20180916|1234 Hocus Potato Way|Tatertown|RI|29801|36085|36085011402|3||||||11||14|12|13||2|2|6||||||||8||||||||3|4|2|2|1|1|85|104|500|4|1|3|1|746|8888|7||9||10|||||NA|8636|8597|2362|9120|18.18|29|NA|65|329|34|2|1|2|1|590943|3|5|24|NA|1|2|12345678|6||||||17||||||2|2|2"

  val validLar2019 =
    "2|95GVQQ61RS6CWQF0SZD9|95GVQQ61RS6CWQF0SZD9F4VRXNN1OCVXHP1JURF9ZJS92|20190914|1|1|2|1|1|101000|1|20190916|1234 Hocus Potato Way|Tatertown|VA|29801|51810|51810040200|3||||||11||14|12|13||2|2|6||||||||8||||||||3|4|2|2|1|1|85|104|500|4|1|3|1|746|8888|7||9||10|||||NA|8636|8597|2362|9120|18.18|29|NA|65|329|34|2|1|2|1|590943|3|5|24|NA|1|2|12345678|6||||||17||||||2|2|2"

  val invalidLar =
    "2|95GVQQ61RS6CWQF0SZD90|95GVQQ61RS6CWQF0SZD9F4VRXNN1OCVXHP1JURF9ZJS92|20180914|1|1|2|1|1|101000|1|20180916|1234 Hocus Potato Way|Tatertown|VA|29801|51810|51810040200|3||||||11||14|12|13||2|2|6||||||||8||||||||3|4|2|2|1|1|85|104|500|4|1|3|1|746|8888|7||9||10|||||NA|8636|8597|2362|9120|18.18|29|NA|65|329|34|2|1|2|1|590943|3|5|24|NA|1|2|12345678|6||||||17||||||2|2|2"

  val file    = multipartFile(csv, "auto-gen-lars.txt")
  val badFile = multipartFile(badCsv, "bad-lars.txt")

  "LAR HTTP Service" must {
    "return OPTIONS" in {
      Options("/lar/parse") ~> larRoutes ~> check {
        status mustBe StatusCodes.OK
      }
    }
    "parse a valid pipe delimited LAR and return JSON representation" in {
      val larValidateRequest = LarValidateRequest(larCsv)
      Post("/lar/parse", larValidateRequest) ~> larRoutes ~> check {
        response.status mustBe StatusCodes.OK
        responseAs[LoanApplicationRegister] mustBe lar
      }
    }

    "fail to parse an invalid pipe delimited LAR and return a list of errors" in {
      val csv       = larGen.sample.getOrElse(LoanApplicationRegister()).toCSV
      val values    = csv.split('|').map(_.trim)
      val badValues = values.head.replace("2", "A") ++ values.tail
      val badCsv    = badValues.mkString("|")
      Post("/lar/parse", LarValidateRequest(badCsv)) ~> larRoutes ~> check {
        status mustBe StatusCodes.BadRequest
        responseAs[HmdaRowParsedErrorSummary].errorMessages.head.fieldName mustBe "LAR Record Identifier"
      }
    }

    "fail to parse a valid pipe delimited LAR with too many fields and return an error" in {
      Post("/lar/parse", LarValidateRequest(larCsv + "|too|many|fields")) ~> larRoutes ~> check {
        status mustBe StatusCodes.BadRequest
        responseAs[HmdaRowParsedErrorSummary].errorMessages.length mustBe 1
      }
    }

    "validate a lar" in {
      Post("/lar/validate/2019", LarValidateRequest(validLar2019)) ~> larRoutes ~> check {
        status mustBe StatusCodes.OK
        responseAs[LoanApplicationRegister] mustBe LarCsvParser(validLar2019)
          .getOrElse(LoanApplicationRegister())
      }
    }

    "fail edits V600 and V623" in {
      Post("/lar/validate/2018", LarValidateRequest(invalidLar)) ~> larRoutes ~> check {
        status mustBe StatusCodes.OK
        responseAs[SingleValidationErrorResult] mustBe SingleValidationErrorResult(
          validity = ValidationErrorSummary(
            Seq(
              ValidationSingleErrorSummary(
                "V600",
                s""""The required format for LEI is alphanumeric with 20 characters, and it cannot be left blank.""""
              )
            )
          )
        )
      }
    }
  }

}