package hmda.persistence.messages.events.institutions

import hmda.model.institution.HmdaFiler
import hmda.persistence.messages.CommonMessages.Event

object HmdaFilerEvents {

  case class HmdaFilerCreated(hmdFiler: HmdaFiler) extends Event
  case class HmdaFilerDeleted(hmdaFiler: HmdaFiler) extends Event
}
