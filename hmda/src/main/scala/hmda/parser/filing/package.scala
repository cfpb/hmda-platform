package hmda.parser

import hmda.model.filing.PipeDelimited
import hmda.parser.ParserErrorModel.ParserValidationError

package object filing {
  type Seq[+A] = scala.collection.immutable.Seq[A]
  type ParseValidated = Either[List[ParserValidationError], PipeDelimited]
}
