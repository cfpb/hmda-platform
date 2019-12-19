package hmda.parser.filing.ts

import hmda.model.filing.ts.TransmittalSheet
import hmda.parser.ParserErrorModel.ParserValidationError
import hmda.parser.filing.lar.LarCsvParser.{controlCharacterFilter, removeBOM, removeTrailingPipe}
import hmda.parser.filing.ts.TsFormatValidator.validateTs

object TsCsvParser {
  def apply(s: String, fromCassandra: Boolean = false): Either[List[ParserValidationError], TransmittalSheet] = {

    val controlCharsRemoved = controlCharacterFilter(s).trim()
    val trailingPipeRemoved = removeTrailingPipe(controlCharsRemoved)
    val cleanTSLine = removeBOM(trailingPipeRemoved)
    val values = cleanTSLine.trim.split("\\|", -1).map(_.trim).toList

    validateTs(values, cleanTSLine, fromCassandra).leftMap(xs => xs.toList).toEither
  }
}
