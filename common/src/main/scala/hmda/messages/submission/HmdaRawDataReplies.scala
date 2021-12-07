package hmda.messages.submission

import hmda.messages.submission.HmdaRawDataEvents.LineAdded

object HmdaRawDataReplies {
  case class LinesAdded(lines: List[LineAdded])
}