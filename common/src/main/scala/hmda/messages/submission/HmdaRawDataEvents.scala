package hmda.messages.submission

import hmda.messages.CommonMessages.Event

object HmdaRawDataEvents {
  sealed trait HmdaRawDataEvent                       extends Event
  case class LineAdded(timestamp: Long, data: String) extends HmdaRawDataEvent
}
