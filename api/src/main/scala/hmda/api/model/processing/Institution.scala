package hmda.api.model.processing

case class Institutions(institutions: Set[Institution])
case class Institution(
  name: String = "",
  id: String = "",
  period: String = "",
  status: ProcessingStatus = ProcessingStatus(),
  currentSubmission: Int = 0
)
