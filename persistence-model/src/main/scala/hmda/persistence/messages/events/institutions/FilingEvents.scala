package hmda.persistence.messages.events.institutions

import hmda.model.fi.Filing
import hmda.persistence.messages.CommonMessages.Event

object FilingEvents {
  sealed trait FilingEvent extends Event
  case class FilingCreated(filing: Filing) extends FilingEvent
  case class FilingStatusUpdated(filing: Filing) extends FilingEvent
}
