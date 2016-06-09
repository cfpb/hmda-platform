package hmda.validation.rules.lar.quality

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.Result
import hmda.validation.rules.EditCheck

object Q051 extends EditCheck[LoanApplicationRegister] {
  override def apply(lar: LoanApplicationRegister): Result = {
    when(notANaturalPerson(lar) and (lar.actionTakenType is equalTo(6))) {
      lar.hoepaStatus not equalTo(1)
    }
  }

  def notANaturalPerson(lar: LoanApplicationRegister): Result = {
    (lar.applicant.ethnicity is equalTo(4)) and
      (lar.applicant.race1 is equalTo(7)) and
      (lar.applicant.sex is equalTo(4))
  }

  override def name = "Q051"
}
