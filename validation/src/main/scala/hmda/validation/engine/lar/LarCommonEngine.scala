package hmda.validation.engine.lar

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.context.ValidationContext
import hmda.validation.engine.{ LarValidationErrors, ValidationError, ValidationErrors }
import hmda.validation.rules.lar.`macro`.MacroEditTypes.LoanApplicationRegisterSource

import scalaz._

trait LarCommonEngine {
  type LarValidation = ValidationNel[ValidationError, LoanApplicationRegister]
  type LarsValidation = ValidationNel[ValidationError, Iterable[LoanApplicationRegister]]
  type LarSourceValidation = ValidationNel[ValidationError, LoanApplicationRegisterSource]

  def validationErrors(lar: LoanApplicationRegister, ctx: ValidationContext, f: (LoanApplicationRegister, ValidationContext) => LarValidation): ValidationErrors = {
    val validation = f(lar, ctx)
    validation match {
      case scalaz.Success(_) => LarValidationErrors(Nil)
      case scalaz.Failure(errors) => LarValidationErrors(errors.list.toList)
    }
  }
}
