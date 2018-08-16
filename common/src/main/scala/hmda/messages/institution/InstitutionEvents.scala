package hmda.messages.institution

import hmda.messages.CommonMessages.Event
import hmda.model.institution.Institution

object InstitutionEvents {

  sealed trait InstitutionEvent extends Event
  case class InstitutionCreated(i: Institution) extends InstitutionEvent
  case class InstitutionModified(i: Institution) extends InstitutionEvent
  case class InstitutionDeleted(LEI: String) extends InstitutionEvent
  case class InstitutionNotExists(LEI: String) extends InstitutionEvent
}
