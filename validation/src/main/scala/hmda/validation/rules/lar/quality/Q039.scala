package hmda.validation.rules.lar.quality

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.model.fi.lar.fields.LarTopLevelFields._
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.Result
import hmda.validation.rules.EditCheck

object Q039 extends EditCheck[LoanApplicationRegister] {
  override def apply(lar: LoanApplicationRegister): Result = {
    when((lar.hoepaStatus is equalTo(1)) and (lar.actionTakenType is equalTo(1))) {
      lar.rateSpread not equalTo("NA")
    }
  }

  override def name = "Q039"

}
