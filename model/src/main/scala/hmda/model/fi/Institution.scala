package hmda.model.fi

trait InstitutionStatus
case object Active extends InstitutionStatus
case object Inactive extends InstitutionStatus

case class Institution(
    id: String = "",
    name: String = "",
    status: InstitutionStatus = Inactive
) {
  def isEmpty: Boolean = {
    if ((id == "") && (name == "") && (status == Inactive)) true
    else false
  }
}
