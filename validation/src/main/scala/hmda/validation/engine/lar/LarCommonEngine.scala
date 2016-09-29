package hmda.validation.engine.lar

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.context.ValidationContext
import hmda.validation.engine.{ ValidationError, ValidationErrors }

import scalaz._

trait LarCommonEngine {
  type LarValidation = ValidationNel[ValidationError, LoanApplicationRegister]
  type LarsValidation = ValidationNel[ValidationError, Iterable[LoanApplicationRegister]]

  def validationErrors(lar: LoanApplicationRegister, ctx: ValidationContext, f: (LoanApplicationRegister, ValidationContext) => LarValidation): ValidationErrors = {
    val validation = f(lar, ctx)
    validation match {
      case scalaz.Success(_) => ValidationErrors(Nil)
      case scalaz.Failure(errors) => ValidationErrors(errors.list.toList)
    }
  }
}