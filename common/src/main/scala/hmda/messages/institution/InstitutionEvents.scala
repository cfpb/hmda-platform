package hmda.messages.institution

import hmda.messages.CommonMessages.Event
import hmda.model.filing.Filing
import hmda.model.institution.Institution

object InstitutionEvents {

  sealed trait InstitutionEvent                               extends Event
  final case class InstitutionCreated(i: Institution)         extends InstitutionEvent
  final case class InstitutionWithLou(i: Institution)         extends InstitutionEvent
  final case class InstitutionModified(i: Institution)        extends InstitutionEvent
  final case class InstitutionDeleted(LEI: String, year: Int) extends InstitutionEvent
  final case class InstitutionNotExists(LEI: String)          extends InstitutionEvent
  final case class FilingAdded(filing: Filing)                extends InstitutionEvent
  final case class InstitutionKafkaEvent(eventType: String, institutionEvent: InstitutionEvent)
}
