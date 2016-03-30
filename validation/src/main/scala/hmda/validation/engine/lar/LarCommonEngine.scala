package hmda.validation.engine.lar

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.engine.ValidationError

import scalaz._

trait LarCommonEngine {
  type LarValidation = ValidationNel[ValidationError, LoanApplicationRegister]
}