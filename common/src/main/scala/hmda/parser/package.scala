package hmda

import cats.data.ValidatedNel
import hmda.parser.ParserErrorModel.ParserValidationError

package object parser {
  type LarParserValidationResult[A] = ValidatedNel[ParserValidationError, A]
}
