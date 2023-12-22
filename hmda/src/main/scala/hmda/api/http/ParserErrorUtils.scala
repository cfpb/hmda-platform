package hmda.api.http.utils

import hmda.model.filing.ParserValidValuesLookup._
import hmda.api.http.model.filing.submissions.{FieldParserErrorSummary, HmdaRowParsedErrorSummary}
import hmda.parser.ParserErrorModel.ParserValidationError
import hmda.messages.submission.SubmissionProcessingEvents._
import hmda.utils.YearUtils.Period

object ParserErrorUtils {

    def parserValidationErrorSummaryConvertor(line: Int, lar: Option[String], errors: List[ParserValidationError]): HmdaRowParsedErrorSummary = {
        HmdaRowParsedErrorSummary(
            line,
            lar match {
              case Some(lar) => estimateULI(lar)
              case None => "Transmittal Sheet"
            }, 
            errors.map(error => 
            FieldParserErrorSummary(
                error.fieldName,
                error.inputValue,
                lookupParserValidValues(error.fieldName)
            )
            ))
    }
  def parserErrorSummaryConvertor(
      hmdaRowParsedError: HmdaRowParsedError,period: Period): HmdaRowParsedErrorSummary = {
    HmdaRowParsedErrorSummary(
      hmdaRowParsedError.rowNumber,
      hmdaRowParsedError.estimatedULI,
      hmdaRowParsedError.errorMessages.map(
        errorMessage =>
          FieldParserErrorSummary(
            errorMessage.fieldName,
            errorMessage.inputValue,
            lookupParserValidValuesByYear(errorMessage.fieldName,period)
        ))
    )
  }

  def estimateULI(line: String): String = {
    val larItems = line.split('|')
    if (larItems.length >= 3) {
      larItems(2)
    } else {
      "The ULI could not be identified."
    }
  }
}