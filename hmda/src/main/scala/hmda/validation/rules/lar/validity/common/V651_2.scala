package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums._
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

object V651_2 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "V651-2"

  override def parent: String = "V651"

  override def apply(lar: LoanApplicationRegister): ValidationResult =
    when(
      (lar.applicant.ethnicity.ethnicity1 is equalTo(EthnicityNotApplicable))
        and (lar.applicant.race.race1 is equalTo(RaceNotApplicable))
        and (lar.applicant.sex.sexEnum is equalTo(SexNotApplicable))
        and (lar.action.actionTakenType not equalTo(PurchasedLoan))
    ) {

      lar.applicant.age is equalTo(8888)
    }
}
