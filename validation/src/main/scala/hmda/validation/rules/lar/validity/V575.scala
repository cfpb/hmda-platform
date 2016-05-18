package hmda.validation.rules.lar.validity

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.dsl.Result
import hmda.validation.rules.EditCheck
import hmda.validation.dsl.PredicateDefaults._
import hmda.validation.dsl.PredicateSyntax._

object V575 extends EditCheck[LoanApplicationRegister] {

  override def name: String = "V575"

  override def apply(lar: LoanApplicationRegister): Result = {
    when(lar.lienStatus is equalTo(2)) {
      lar.rateSpread is equalTo("NA") or (lar.rateSpread is numericallyBetween("3.50", "99.99"))
    }
  }
}
