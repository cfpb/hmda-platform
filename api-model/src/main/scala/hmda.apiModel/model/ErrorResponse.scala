package hmda.apiModel.model

import akka.http.scaladsl.model.Uri.Path

case class ErrorResponse(
  httpStatus: Int,
  message: String,
  path: Path
)
