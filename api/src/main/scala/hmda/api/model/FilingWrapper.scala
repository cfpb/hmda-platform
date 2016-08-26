package hmda.api.model

case class SubmissionWrapper(
  id: Int,
  status: SubmissionStatusWrapper
)

case class SubmissionStatusWrapper(
  code: Int,
  message: String
)
