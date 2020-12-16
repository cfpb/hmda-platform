package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.uli.validation.ULI
import hmda.validation.dsl.{ ValidationFailure, ValidationResult, ValidationSuccess }
import hmda.validation.rules.EditCheck

object V609 extends EditCheck[LoanApplicationRegister] {

  override def name: String = "V609"

  override def apply(lar: LoanApplicationRegister): ValidationResult = {

    val uli = lar.loan.ULI

    if (uli.length <= 22) {
      ValidationSuccess
    } else {
      if (ULI.validateULIViaEdits(uli)) {
        ValidationSuccess
      } else {
        ValidationFailure
      }
    }
  }
}
