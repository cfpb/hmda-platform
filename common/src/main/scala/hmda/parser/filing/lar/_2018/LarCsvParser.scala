package hmda.parser.filing.lar._2018

import hmda.model.filing.lar._2018.LoanApplicationRegister
import hmda.parser.ParserErrorModel.ParserValidationError

object LarCsvParser {
  def apply(s: String, fromCassandra: Boolean = false)
    : Either[List[ParserValidationError], LoanApplicationRegister] = {
    val values = s.trim.split('|').map(_.trim).toList
    validateLar(values, s, fromCassandra).leftMap(xs => xs.toList).toEither
  }
}
