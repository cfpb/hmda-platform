package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums._
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

object V655_2 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "V655-2"

  override def parent: String = "V655"

  override def apply(lar: LoanApplicationRegister): ValidationResult =
    when(
      (lar.coApplicant.ethnicity.ethnicity1 is equalTo(EthnicityNotApplicable))
        and (lar.coApplicant.race.race1 is equalTo(RaceNotApplicable))
        and (lar.coApplicant.sex.sexEnum is equalTo(SexNotApplicable))
        and (lar.action.actionTakenType not equalTo(PurchasedLoan))
    ) {

      lar.income is equalTo("NA")
    }
}
