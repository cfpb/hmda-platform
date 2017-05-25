package hmda.persistence.messages.events.processing

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.model.fi.ts.TransmittalSheet
import hmda.model.parser.LarParsingError
import hmda.persistence.messages.CommonMessages.Event

object HmdaFileParserEvents {
  trait HmdaFileParserEvent extends Event
  case class TsParsed(ts: TransmittalSheet) extends HmdaFileParserEvent
  case class TsParsedErrors(errors: List[String]) extends HmdaFileParserEvent
  case class LarParsed(lar: LoanApplicationRegister) extends HmdaFileParserEvent
  case class LarParsedErrors(errors: LarParsingError) extends HmdaFileParserEvent
}
