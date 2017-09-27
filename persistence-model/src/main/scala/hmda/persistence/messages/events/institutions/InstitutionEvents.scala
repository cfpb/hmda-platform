package hmda.persistence.messages.events.institutions

import hmda.model.institution.Institution
import hmda.persistence.messages.CommonMessages._

object InstitutionEvents {
  trait InstitutionEvent extends Event
  case class InstitutionCreated(institution: Institution) extends InstitutionEvent
  case class InstitutionModified(institution: Institution) extends InstitutionEvent
}
