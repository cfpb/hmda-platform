package hmda.api.http.codec

import akka.http.scaladsl.model.Uri.Path
import hmda.api.http.model.ErrorResponse
import io.circe.Decoder.Result
import io.circe.{ Decoder, Encoder, HCursor, Json }

object ErrorResponseCodec {

  implicit val errorResponseEncoder: Encoder[ErrorResponse] =
    new Encoder[ErrorResponse] {
      override def apply(a: ErrorResponse): Json = Json.obj(
        ("httpStatus", Json.fromInt(a.httpStatus)),
        ("message", Json.fromString(a.message)),
        ("path", Json.fromString(a.path.toString()))
      )
    }

  implicit val errorResponseDecoder: Decoder[ErrorResponse] =
    new Decoder[ErrorResponse] {
      override def apply(c: HCursor): Result[ErrorResponse] =
        for {
          httpStatus <- c.downField("httpStatus").as[Int]
          message    <- c.downField("message").as[String]
          pathStr    <- c.downField("path").as[String]
        } yield {
          ErrorResponse(httpStatus, message, Path.apply(pathStr))
        }
    }

}
