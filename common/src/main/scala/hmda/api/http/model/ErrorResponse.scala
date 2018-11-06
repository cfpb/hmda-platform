package hmda.api.http.model

import akka.http.scaladsl.model.Uri.Path

object ErrorResponse {
  def apply(): ErrorResponse =
    ErrorResponse(500, "Internal Server Error", Path.apply(""))
}

case class ErrorResponse(
    httpStatus: Int,
    message: String,
    path: Path
)
