package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums._
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

object V652_2 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "V652-2"

  override def parent: String = "V652"

  override def apply(lar: LoanApplicationRegister): ValidationResult = {
    when(
      (lar.coApplicant.ethnicity.ethnicity1 is equalTo(EthnicityNotApplicable))
        and (lar.coApplicant.race.race1 is equalTo(RaceNotApplicable))
        and (lar.coApplicant.sex.sexEnum is equalTo(SexNotApplicable))) {

      lar.coApplicant.age is equalTo(8888)
    }
  }
}
