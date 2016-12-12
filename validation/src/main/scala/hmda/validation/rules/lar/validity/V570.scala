package hmda.validation.rules.lar.validity

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.model.fi.lar.fields.LarTopLevelFields._
import hmda.validation.dsl.Result
import hmda.validation.rules.EditCheck
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._

object V570 extends EditCheck[LoanApplicationRegister] {

  override def name: String = "V570"

  override def apply(lar: LoanApplicationRegister): Result = {
    when(lar.lienStatus is equalTo(1)) {
      (lar.rateSpread is equalTo("NA")) or (lar.rateSpread is numericallyBetween("1.50", "99.99"))
    }
  }
}
