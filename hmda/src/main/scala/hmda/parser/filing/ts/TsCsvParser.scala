package hmda.parser.filing.ts

import hmda.model.filing.ts.TransmittalSheet
import hmda.parser.ParserErrorModel.ParserValidationError
import TsFormatValidator._

object TsCsvParser {
  def apply(
      s: String): Either[List[ParserValidationError], TransmittalSheet] = {
    val values = s.split("\\|", -1).map(_.trim).toList
    validateTs(values).leftMap(xs => xs.toList).toEither
  }
}
