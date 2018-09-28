package hmda.messages.filing

import hmda.messages.CommonMessages.Event
import hmda.model.filing.Filing

object FilingEvents {
  sealed trait FilingEvent extends Event
  case class FilingCreated(filing: Filing) extends FilingEvent
  case class FilingStatusUpdated(filing: Filing) extends FilingEvent
}
