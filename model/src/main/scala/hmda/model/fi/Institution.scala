package hmda.model.fi

trait InstitutionStatus
case object Active extends InstitutionStatus
case object Inactive extends InstitutionStatus

sealed trait PossibleInstitution

case object InstitutionNotFound extends PossibleInstitution

case class Institution(
  id: String = "",
  name: String = "",
  status: InstitutionStatus = Inactive
) extends PossibleInstitution
