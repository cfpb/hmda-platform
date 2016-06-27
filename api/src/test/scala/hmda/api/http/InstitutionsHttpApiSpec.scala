package hmda.api.http

import akka.event.{ LoggingAdapter, NoLogging }
import akka.http.scaladsl.model.{ ContentTypes, HttpEntity, Multipart, StatusCodes }
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.util.Timeout
import hmda.api.persistence.InstitutionPersistence._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import hmda.api.demo.DemoData
import hmda.api.model._
import hmda.model.fi._
import org.scalatest.{ BeforeAndAfterAll, MustMatchers, WordSpec }

import scala.concurrent.duration._

class InstitutionsHttpApiSpec extends WordSpec with MustMatchers with ScalatestRouteTest with InstitutionsHttpApi with BeforeAndAfterAll {
  override val log: LoggingAdapter = NoLogging
  override implicit val timeout: Timeout = Timeout(5.seconds)

  val ec = system.dispatcher

  override def beforeAll(): Unit = {
    createInstitutions(system)

    DemoData.loadData(system)
  }

  "Institutions HTTP API" must {
    "return a list of existing institutions" in {
      Get("/institutions") ~> institutionsRoutes ~> check {
        status mustBe StatusCodes.OK
        responseAs[Institutions] mustBe Institutions(DemoData.institutions)
      }
    }

    "return an institution by id" in {
      Get("/institutions/12345") ~> institutionsRoutes ~> check {
        status mustBe StatusCodes.OK
        val institution = DemoData.institutions.head
        val filings = DemoData.filings.filter(f => f.fid == institution.id).reverse
        responseAs[InstitutionDetail] mustBe InstitutionDetail(institution, filings)
      }
    }

    "return an institution's summary" in {
      Get("/institutions/12345/summary") ~> institutionsRoutes ~> check {
        status mustBe StatusCodes.OK
        responseAs[InstitutionSummary] mustBe DemoData.institutionSummary
      }
    }

    "return a list of submissions for a financial institution" in {
      Get("/institutions/12345/filings/2017") ~> institutionsRoutes ~> check {
        status mustBe StatusCodes.OK
        val filing = Filing("2017", "12345", NotStarted)
        responseAs[FilingDetail] mustBe FilingDetail(filing, DemoData.newSubmissions.reverse)
      }
    }

    "create a new submission" in {
      Post("/institutions/12345/filings/2017/submissions") ~> institutionsRoutes ~> check {
        status mustBe StatusCodes.Created
        responseAs[Submission] mustBe Submission(DemoData.newSubmissions.size + 1, Created)
      }
    }

    "return proper response when uploading a HMDA file" in {
      val csv = "1|0123456789|9|201301171330|2013|99-9999999|900|MIKES SMALL BANK   XXXXXXXXXXX|1234 Main St       XXXXXXXXXXXXXXXXXXXXX|Sacramento         XXXXXX|CA|99999-9999|MIKES SMALL INC    XXXXXXXXXXX|1234 Kearney St    XXXXXXXXXXXXXXXXXXXXX|San Francisco      XXXXXX|CA|99999-1234|Mrs. Krabappel     XXXXXXXXXXX|916-999-9999|999-753-9999|krabappel@gmail.comXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX\n" +
        "2|0123456789|9|ABCDEFGHIJKLMNOPQRSTUVWXY|20130117|4|3|2|1|10000|1|5|20130119|06920|06|034|0100.01|4|5|7|4|3|2|1|8|7|6|5|4|1|2|9000|0|9|8|7|01.05|2|4\n" +
        "2|0123456789|9|ABCDEFGHIJKLMNOPQRSTUVWXY|20130117|4|3|2|1|10000|1|5|20130119|06920|06|034|0100.01|4|5|7|4|3|2|1|8|7|6|5|4|1|2|9000|0|9|8|7|01.05|2|4\n" +
        "2|0123456789|9|ABCDEFGHIJKLMNOPQRSTUVWXY|20130117|4|3|2|1|10000|1|5|20130119|06920|06|034|0100.01|4|5|7|4|3|2|1|8|7|6|5|4|1|2|9000|0|9|8|7|01.05|2|4"

      val file = multiPartFile(csv, "sample.txt")

      Post("/institutions/12345/filings/2017/submissions/1", file) ~> institutionsRoutes ~> check {
        status mustBe StatusCodes.OK
        responseAs[String] mustBe "uploaded"
      }
    }

    "return 400 when trying to upload the wrong file" in {
      val badContent = "qdemd"
      val file = multiPartFile(badContent, "sample.dat")
      Post("/institutions/12345/filings/2017/submissions/1", file) ~> institutionsRoutes ~> check {
        status mustBe StatusCodes.BadRequest
      }
    }

  }

  private def multiPartFile(contents: String, fileName: String) = {
    Multipart.FormData(Multipart.FormData.BodyPart.Strict(
      "file",
      HttpEntity(ContentTypes.`text/plain(UTF-8)`, contents),
      Map("filename" -> fileName)
    ))
  }

}
