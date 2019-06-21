package hmda.parser.filing.ts._2018

import hmda.model.filing.ts._2018.TransmittalSheet
import hmda.parser.ParserErrorModel.ParserValidationError

object TsCsvParser {
  def apply(
      s: String): Either[List[ParserValidationError], TransmittalSheet] = {
    val values = s.trim.split("\\|", -1).map(_.trim).toList
    validateTs(values, s).leftMap(xs => xs.toList).toEither
  }
}
