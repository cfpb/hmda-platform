package hmda.parser

import hmda.parser.ParserErrorModel.ParserValidationError

package object filing {
  type Seq[+A]            = scala.collection.immutable.Seq[A]
  type ParseValidated[+A] = Either[List[ParserValidationError], A]
}
