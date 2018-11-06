package hmda.api.http.codec

import akka.http.scaladsl.model.Uri.Path
import hmda.api.http.model.ErrorResponse
import org.scalatest.{MustMatchers, WordSpec}
import ErrorResponseCodec._
import io.circe.syntax._

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
