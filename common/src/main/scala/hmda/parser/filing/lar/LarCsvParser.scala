package hmda.parser.filing.lar

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.parser.ParserErrorModel.ParserValidationError
import hmda.parser.filing.lar.LarFormatValidator.validateLar

object LarCsvParser {
  def apply(s: String)
    : Either[List[ParserValidationError], LoanApplicationRegister] = {
    val values = s.split('|').map(_.trim).toList
    validateLar(values).leftMap(xs => xs.toList).toEither
  }
}
