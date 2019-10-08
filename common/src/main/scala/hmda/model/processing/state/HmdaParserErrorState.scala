package hmda.model.processing.state

import hmda.messages.submission.SubmissionProcessingEvents.HmdaRowParsedError

case class HmdaParserErrorState(transmittalSheetErrors: Seq[HmdaRowParsedError] = Nil,
                                larErrors: Seq[HmdaRowParsedError] = Nil,
                                totalErrors: Int = 0) {
  def update(parserError: HmdaRowParsedError): HmdaParserErrorState = {
    val newTsErrors =
      if (parserError.rowNumber == 1) this.transmittalSheetErrors :+ parserError
      else this.transmittalSheetErrors
    val newLarErrors =
      if (parserError.rowNumber != 1) this.larErrors :+ parserError
      else this.larErrors
    HmdaParserErrorState(newTsErrors, newLarErrors, totalErrors + 1)
  }
}
