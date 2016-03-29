package hmda.api.http

import akka.event.{ NoLogging, LoggingAdapter }
import akka.http.scaladsl.model._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import hmda.api.model.Status
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import org.scalatest.{ MustMatchers, WordSpec }

import scala.concurrent.ExecutionContext

class HttpApiSpec extends WordSpec with MustMatchers with ScalatestRouteTest with HttpApi {
  override val log: LoggingAdapter = NoLogging
  val ec: ExecutionContext = system.dispatcher

  "Http API service" should {

    "return OK for GET requests to the root path" in {
      Get() ~> routes ~> check {
        responseAs[Status].status mustEqual "OK"
        responseAs[Status].service mustEqual "hmda-api"
      }
    }

    "return OK when uploading a HMDA file" in {
      val csv = "1|0123456789|9|201301171330|2013|99-9999999|900|MIKES SMALL BANK   XXXXXXXXXXX|1234 Main St       XXXXXXXXXXXXXXXXXXXXX|Sacramento         XXXXXX|CA|99999-9999|MIKES SMALL INC    XXXXXXXXXXX|1234 Kearney St    XXXXXXXXXXXXXXXXXXXXX|San Francisco      XXXXXX|CA|99999-1234|Mrs. Krabappel     XXXXXXXXXXX|916-999-9999|999-753-9999|krabappel@gmail.comXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX\n" +
        "2|0123456789|9|ABCDEFGHIJKLMNOPQRSTUVWXY|20130117|4|3|2|1|10000|1|5|20130119|06920|06|034|0100.01|4|5|7|4|3|2|1|8|7|6|5|4|1|2|9000|0|9|8|7|01.05|2|4\n" +
        "2|0123456789|9|ABCDEFGHIJKLMNOPQRSTUVWXY|20130117|4|3|2|1|10000|1|5|20130119|06920|06|034|0100.01|4|5|7|4|3|2|1|8|7|6|5|4|1|2|9000|0|9|8|7|01.05|2|4\n" +
        "2|0123456789|9|ABCDEFGHIJKLMNOPQRSTUVWXY|20130117|4|3|2|1|10000|1|5|20130119|06920|06|034|0100.01|4|5|7|4|3|2|1|8|7|6|5|4|1|2|9000|0|9|8|7|01.05|2|4"

      val file = multiPartFile(csv, "sample.txt")

      Post("/upload/1", file) ~> routes ~> check {
        status mustEqual StatusCodes.OK
      }
    }

    "return 400 when trying to upload the wrong file" in {
      val badContent = "qdemd"
      val file = multiPartFile(badContent, "sample.dat")
      Post("/upload/0123456789", file) ~> routes ~> check {
        status mustEqual StatusCodes.BadRequest
      }
    }

  }

  private def multiPartFile(contents: String, fileName: String) = {
    Multipart.FormData(Multipart.FormData.BodyPart.Strict(
      "field1",
      HttpEntity(ContentTypes.`text/plain(UTF-8)`, contents),
      Map("filename" -> fileName)
    ))
  }

}
