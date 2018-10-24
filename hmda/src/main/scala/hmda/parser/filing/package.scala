package hmda.parser

import hmda.model.filing.HmdaFileRow
import hmda.parser.ParserErrorModel.ParserValidationError

package object filing {
  type Seq[+A] = scala.collection.immutable.Seq[A]
  type ParseValidated = Either[List[ParserValidationError], HmdaFileRow]
}
