package hmda.messages.submission

import hmda.messages.CommonMessages.Event
import hmda.model.filing.ts.TransmittalSheet

object HmdaParserErrorEvents {

  sealed trait HmdaParserErrorEvent extends Event

  case class TsParsed(ts: TransmittalSheet) extends HmdaParserErrorEvent
}
