package hmda.persistence.institution

import hmda.model.institution.Institution

case class InstitutionState(institution: Option[Institution]) {
  def isEmpty: Boolean = institution.isEmpty
}
