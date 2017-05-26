package hmda.persistence.serialization.parser

import hmda.model.parser.LarParsingError
import hmda.persistence.messages.events.processing.HmdaFileParserEvents.{ LarParsed, LarParsedErrors, TsParsed, TsParsedErrors }
import hmda.persistence.model.serialization.HmdaFileParserEvents._
import hmda.persistence.model.serialization.LoanApplicationRegister.LoanApplicationRegisterMessage
import hmda.persistence.model.serialization.TransmittalSheet.TransmittalSheetMessage
import hmda.persistence.serialization.ts.TsProtobufConverter._
import hmda.persistence.serialization.lar.LARProtobufConverter._

object HmdaFileParserProtobufConverter {

  def tsParsedToProtobuf(event: TsParsed): TsParsedMessage = {
    TsParsedMessage(
      ts = Some(tsToProtobuf(event.ts))
    )
  }
  def tsParsedFromProtobuf(msg: TsParsedMessage): TsParsed = {
    TsParsed(
      ts = tsFromProtobuf(msg.ts.getOrElse(TransmittalSheetMessage()))
    )
  }

  def tsParsedErrorsToProtobuf(event: TsParsedErrors): TsParsedErrorsMessage = {
    TsParsedErrorsMessage(
      errors = event.errors
    )
  }
  def tsParsedErrorsFromProtobuf(msg: TsParsedErrorsMessage): TsParsedErrors = {
    TsParsedErrors(
      errors = msg.errors.toList
    )
  }

  def larParsedToProtobuf(event: LarParsed): LarParsedMessage = {
    LarParsedMessage(
      lar = Some(loanApplicationRegisterToProtobuf(event.lar))
    )
  }
  def larParsedFromProtobuf(msg: LarParsedMessage): LarParsed = {
    LarParsed(
      lar = loanApplicationRegisterFromProtobuf(msg.lar.getOrElse(LoanApplicationRegisterMessage()))
    )
  }

  def larParsingErrorToProtobuf(obj: LarParsingError): LarParsingErrorMessage = {
    LarParsingErrorMessage(
      lineNumber = obj.lineNumber,
      errorMessages = obj.errorMessages
    )
  }
  def larParsingErrorFromProtobuf(msg: LarParsingErrorMessage): LarParsingError = {
    LarParsingError(
      lineNumber = msg.lineNumber,
      errorMessages = msg.errorMessages.toList
    )
  }

  def larParsedErrorsToProtobuf(event: LarParsedErrors): LarParsedErrorsMessage = {
    LarParsedErrorsMessage(
      errors = Some(larParsingErrorToProtobuf(event.errors))
    )
  }
  def larParsedErrorsFromProtobuf(msg: LarParsedErrorsMessage): LarParsedErrors = {
    LarParsedErrors(
      errors = larParsingErrorFromProtobuf(msg.errors.getOrElse(LarParsingErrorMessage()))
    )
  }

}
