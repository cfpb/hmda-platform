package hmda.api.http.codec

import akka.http.scaladsl.model.Uri.Path
import hmda.api.http.model.ErrorResponse
import io.circe.syntax._
import org.scalatest.{ MustMatchers, WordSpec }

class ErrorResponseCodecSpec extends WordSpec with MustMatchers {

  "An Error Response" must {
    "Serialize to and from JSON" in {
      val errorResponse =
        ErrorResponse(500, "Internal Server Error", Path.apply("/some/path"))
      val json = errorResponse.asJson
      json.as[ErrorResponse].getOrElse(ErrorResponse()) mustBe errorResponse
    }
  }

}