package hmda.parser.filing.ts

import hmda.model.filing.ts.TransmittalSheet
import hmda.parser.ParserErrorModel.ParserValidationError
import hmda.parser.filing.ts.TsFormatValidator.validateTs

object TsCsvParser {
  def apply(s: String, fromCassandra: Boolean = false): Either[List[ParserValidationError], TransmittalSheet] = {
    val values = s.trim.split("\\|", -1).map(_.trim).toList
    validateTs(values, s, fromCassandra).leftMap(xs => xs.toList).toEither
  }
}
