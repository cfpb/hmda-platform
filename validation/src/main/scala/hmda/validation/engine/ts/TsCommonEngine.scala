package hmda.validation.engine.ts

import hmda.model.fi.ts.TransmittalSheet
import hmda.validation.engine.ValidationError

import scala.concurrent.ExecutionContext
import scalaz._

trait TsCommonEngine {
  type TsValidation = ValidationNel[ValidationError, TransmittalSheet]
  implicit val ec: ExecutionContext
}
