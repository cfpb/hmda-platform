package hmda.parser.filing.lar

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.parser.ParserErrorModel.ParserValidationError
import hmda.parser.filing.lar.LarFormatValidator.validateLar

object LarCsvParser {
  def apply(s: String, fromCassandra: Boolean = false): Either[List[ParserValidationError], LoanApplicationRegister] = {
    val values = s.trim.split('|').map(_.trim).toList
    validateLar(values, s, fromCassandra).leftMap(xs => xs.toList).toEither
  }
}
