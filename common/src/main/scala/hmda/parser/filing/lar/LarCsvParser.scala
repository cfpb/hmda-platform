package hmda.parser.filing.lar

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.parser.ParserErrorModel.ParserValidationError
import hmda.parser.filing.lar.LarFormatValidator.validateLar
import hmda.util.conversion.ColumnDataFormatter

object LarCsvParser extends ColumnDataFormatter {
  def apply(s: String, fromCassandra: Boolean = false): Either[List[ParserValidationError], LoanApplicationRegister] = {

    val controlCharsRemoved = controlCharacterFilter(s).trim()
    val trailingPipeRemoved = removeTrailingLARPipe(controlCharsRemoved)
    val cleanLarLine = removeBOM(trailingPipeRemoved)
    val values = cleanLarLine.trim.split('|').map(_.trim).toList

    validateLar(values, cleanLarLine, fromCassandra).leftMap(xs => xs.toList).toEither
  }
}
