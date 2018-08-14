package hmda.messages.institution

import hmda.model.institution.Institution

object InstitutionEvents {

  sealed trait InstitutionEvent
  case class InstitutionCreated(i: Institution) extends InstitutionEvent
  case class InstitutionModified(i: Institution) extends InstitutionEvent
  case class InstitutionDeleted(LEI: String) extends InstitutionEvent
  case class InstitutionNotExists(LEI: String) extends InstitutionEvent
}
