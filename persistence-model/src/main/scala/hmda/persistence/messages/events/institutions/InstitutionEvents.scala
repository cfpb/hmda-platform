package hmda.persistence.messages.events.institutions

import hmda.model.institution.Institution
import hmda.persistence.messages.CommonMessages._

object InstitutionEvents {
  trait InstitutionEvent extends Event
  case class InstitutionSchemaCreated() extends InstitutionEvent
  case class InstitutionSchemaDeleted() extends InstitutionEvent
  case class InstitutionCreated(i: Institution) extends InstitutionEvent
  case class InstitutionModified(i: Institution) extends InstitutionEvent
}
