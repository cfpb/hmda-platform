package hmda.validation.rules.lar.validity._2027

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.validity.{ V671_1, V671_2 }


object V671 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "V671"

  override def apply(lar: LoanApplicationRegister): ValidationResult =
    V671_1.apply(lar) and V671_2.apply(lar)
}
