package hmda.persistence.messages.events.apor

import java.time.LocalDate

import hmda.model.apor.{ APOR, RateType }
import hmda.persistence.messages.CommonMessages.Event

object APOREvents {
  case class AporCreated(apor: APOR, rateType: RateType) extends Event
  case class AporModified(date: LocalDate, rateType: RateType, aPOR: APOR) extends Event
}
