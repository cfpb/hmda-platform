package hmda.validation.rules.lar.validity

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.dsl.{ RegexDsl, Result }
import hmda.validation.rules.EditCheck

object V500 extends EditCheck[LoanApplicationRegister] with RegexDsl {
  override def name: String = "V500"

  override def apply(lar: LoanApplicationRegister): Result = {
    (lar.rateSpread is equalTo("NA")) or (lar.rateSpread is numericMatching)
  }
}
