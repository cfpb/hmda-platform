package hmda.model.processing.state

import hmda.messages.submission.SubmissionProcessingEvents.HmdaRowParsedError

case class HmdaParserErrorState(tsParsingErrors: Seq[HmdaRowParsedError] = Nil,
                                larParsingErrors: Seq[HmdaRowParsedError] = Nil,
                                totalErrors: Int = 0) {
  def update(parserError: HmdaRowParsedError): HmdaParserErrorState = {
    val newTsErrors =
      if (parserError.rowNumber == 1) this.tsParsingErrors :+ parserError
      else this.tsParsingErrors
    val newLarErrors =
      if (parserError.rowNumber != 1) this.larParsingErrors :+ parserError
      else this.larParsingErrors
    HmdaParserErrorState(newTsErrors, newLarErrors, totalErrors + 1)
  }
}
