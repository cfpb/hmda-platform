package hmda.validation.rules.lar.quality

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.Result
import hmda.validation.rules.EditCheck

object Q030 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "Q030"

  override def apply(lar: LoanApplicationRegister): Result = {
    when(lar.actionTakenType is containedIn(1 to 6)) {
      (lar.geography.msa not "NA") and
        (lar.geography.tract not "NA") and
        (lar.geography.county not "NA") and
        (lar.geography.state not "NA")
    }
  }
}
