package hmda.validation.rules.lar.validity

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.dsl.Result
import hmda.validation.rules.EditCheck
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._

object V535 extends EditCheck[LoanApplicationRegister] {

  override def apply(lar: LoanApplicationRegister): Result = {
    when(
      (lar.applicant.ethnicity is equalTo(4))
        and (lar.applicant.race1 is equalTo(7))
        and (lar.applicant.sex is equalTo(4))
        and (lar.actionTakenType not equalTo(6))
    ) {
        (lar.hoepaStatus not equalTo(1))
      }
  }

  override def name = "V535"
}
