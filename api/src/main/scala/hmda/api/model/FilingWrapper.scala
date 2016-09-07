package hmda.api.model

case class SubmissionWrapper(
  id: String,
  status: SubmissionStatusWrapper
)

case class SubmissionStatusWrapper(
  code: Int,
  message: String
)
