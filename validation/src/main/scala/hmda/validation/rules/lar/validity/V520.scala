package hmda.validation.rules.lar.validity

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.dsl.Result
import hmda.validation.rules.EditCheck
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._

object V520 extends EditCheck[LoanApplicationRegister] {

  override def name: String = "V520"

  override def apply(lar: LoanApplicationRegister): Result = {
    when(lar.lienStatus is 3) {
      lar.rateSpread is "NA"
    }
  }
}
