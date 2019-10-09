package hmda.api.http.model

import akka.http.scaladsl.model.Uri.Path
import io.circe.Decoder.Result
import io.circe.{Decoder, Encoder, HCursor, Json}

object ErrorResponse {
  def apply(): ErrorResponse =
    ErrorResponse(500, "Internal Server Error", Path.apply(""))

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
          message <- c.downField("message").as[String]
          pathStr <- c.downField("path").as[String]
        } yield {
          ErrorResponse(httpStatus, message, Path.apply(pathStr))
        }
    }

}

case class ErrorResponse(
  httpStatus: Int,
  message: String,
  path: Path
)
