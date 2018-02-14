package hmda.validation.engine.ts

import hmda.model.fi.ts.TransmittalSheet
import hmda.model.validation.ValidationError
import hmda.validation.context.ValidationContext
import hmda.validation.engine.{ TsValidationErrors, ValidationErrors }

import scala.concurrent.ExecutionContext
import scalaz._

trait TsCommonEngine {
  type TsValidation = ValidationNel[ValidationError, TransmittalSheet]
  implicit val ec: ExecutionContext

  def validationErrors(ts: TransmittalSheet, ctx: ValidationContext, f: (TransmittalSheet, ValidationContext) => TsValidation): ValidationErrors = {
    val validation = f(ts, ctx)
    validation match {
      case scalaz.Success(_) => TsValidationErrors(Nil)
      case scalaz.Failure(errors) => TsValidationErrors(errors.list.toList)
    }
  }
}
