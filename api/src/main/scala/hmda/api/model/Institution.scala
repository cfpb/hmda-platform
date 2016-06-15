package hmda.api.model

trait InstitutionStatus
case object Active extends InstitutionStatus
case object Inactive extends InstitutionStatus

case class Institution(
  id: String = "",
  name: String = "",
  status: InstitutionStatus = Inactive
)
