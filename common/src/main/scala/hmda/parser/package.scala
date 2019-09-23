package hmda

import cats.data.ValidatedNel
import hmda.parser.ParserErrorModel.ParserValidationError

package object parser {
  type Seq[+A]                      = scala.collection.immutable.Seq[A]
  type LarParserValidationResult[A] = ValidatedNel[ParserValidationError, A]
}
