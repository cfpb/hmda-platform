package hmda.api.model

case class ErrorResponse(
  httpStatus: Int,
  message: String,
  parth: String
)
