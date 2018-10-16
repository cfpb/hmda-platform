package hmda.api.http.public

import akka.event.{LoggingAdapter, NoLogging}
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.testkit.{RouteTestTimeout, ScalatestRouteTest}
import akka.util.Timeout
import hmda.api.http.model.public.ValidatedResponse
import hmda.model.filing.lar.LarGenerators.larNGen
import hmda.model.filing.ts.TransmittalSheet
import hmda.model.filing.ts.TsGenerators.tsGen
import org.scalatest.{MustMatchers, WordSpec}
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe.generic.auto._
import akka.http.scaladsl.unmarshalling.Unmarshaller._
import hmda.util.http.FileUploadUtils

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import akka.testkit._

class HmdaFileValidationHttpApiSpec
    extends WordSpec
    with MustMatchers
    with ScalatestRouteTest
    with HmdaFileValidationHttpApi
    with FileUploadUtils {

  override val log: LoggingAdapter = NoLogging
  val ec: ExecutionContext = system.dispatcher
  val duration = 5.seconds
  implicit val timeout = Timeout(duration)
  implicit val routeTimeout = RouteTestTimeout(duration.dilated)

  val lars = larNGen(10).sample.getOrElse(Nil)
  val ts = tsGen.sample.getOrElse(TransmittalSheet())

  val tsCsv = s"${ts.toCSV}\n"
  val larsCsv = lars.map(lar => s"${lar.toCSV}")
  val badLar =
    "2|AYAKEFD53DRJIQYNCI0U||a|4|5|2|2|1|16041|2|20181217|1234 Hocus Potato Way|Tatertown|UT|84096|49035|49035111906|1|||||8GMCACAP36H23X5P4CY43EKEW9U99R4VGENRVJ26M7YPH9U7O9PCXLZAWN08ZEVNW5GMGT|14|11|13|1|12|IN376P|1|2|2|3|5|4|4|XI523E3TOC6AA3J7IWQYPHNA7XQF6VZHLC5YMDLSOANWCJLP69S2PRIWV1L4W|AZAQ11|Y2O3RJE1NNVPLQIKPOVK2QTCPX367O8I2XGGW2854INVZGIMPYBANX82JFNOD7NP1PU|6|||||YL0MF86ZDTHSY4IR9XTN0943NWGII7N74ZWBVEYDFIAZ|IQS0OJCR|1NINW4K9VEJPOLIX3H3Z430MTXXIQAKVFTVXSOU7ITFO905GR2L58J4IBDGO0I0KVPUDRO9O21IPZATYBE3MQI|1|3|2|3|1|2|36|59|NA|0|NA|3|2|392|581|8|ZUXWM29BLH3MY74D09ZTO16HVBXJBY5DQ800PHRA2TCX8J6EV0KYFDAA17E2DIHBDOP|7||10|||||NA|NA|NA|NA|NA|9.7|NA|NA|188|NA|21|1|1|1|1|15962|3|5|28|17|2|2|LMIS8LM|3||||1||1||||2||1|1|2"
  val badCsv = tsCsv + s"$badLar\n" + larsCsv.mkString("\n")
  val badFile = multipartFile(badCsv, "bad-lars.txt")

  "HMDA File HTTP Service" must {
    "return OPTIONS" in {
      Options("/hmda/parse") ~> hmdaFileRoutes ~> check {
        status mustBe StatusCodes.OK
      }
      Options("/hmda/parse/csv") ~> hmdaFileRoutes ~> check {
        status mustBe StatusCodes.OK
      }
    }
    "parse a HMDA file" in {
      Post("/hmda/parse", badFile) ~> hmdaFileRoutes ~> check {
        status mustBe StatusCodes.OK
        val result = responseAs[ValidatedResponse]
        result.validated.size mustBe 1
      }
    }

    "parse a HMDA file and return a CSV" in {
      Post("/hmda/parse/csv", badFile) ~> hmdaFileRoutes ~> check {
        status mustBe StatusCodes.OK
        val csv = responseAs[String]
        println(csv)
        csv must include("lineNumber|errors")
        csv must include("2|application date is not numeric")
      }
    }
  }
}
