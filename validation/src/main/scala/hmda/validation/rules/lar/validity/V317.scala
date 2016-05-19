package hmda.validation.rules.lar.validity

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.dsl.Result
import hmda.validation.rules.EditCheck
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._

object V317 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "V317"

  override def apply(lar: LoanApplicationRegister): Result = {
    when(lar.applicant.coRace1 is equalTo(8)) {
      (lar.applicant.coSex is equalTo(5)) and
        (lar.applicant.coEthnicity is equalTo(5))
    }
  }
}
