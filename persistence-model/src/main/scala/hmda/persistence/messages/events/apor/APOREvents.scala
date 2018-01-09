package hmda.persistence.messages.events.apor

import hmda.model.apor.{ APOR, RateType }
import hmda.persistence.messages.CommonMessages.Event

object APOREvents {
  case class AporCreated(apor: APOR, rateType: RateType) extends Event
  case class AporModified(rateType: RateType, newApor: APOR) extends Event
}
