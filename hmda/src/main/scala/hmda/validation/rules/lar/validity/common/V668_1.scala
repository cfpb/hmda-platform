package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums._
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

object V668_1 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "V668-1"

  override def parent: String = "V668"

  override def apply(lar: LoanApplicationRegister): ValidationResult =
    when(
      lar.applicant.ethnicity.ethnicity1 is equalTo(EthnicityNotApplicable) and
        (lar.applicant.race.race1 is equalTo(RaceNotApplicable)) and
        (lar.applicant.sex.sexEnum is equalTo(SexNotApplicable))
    ) {
      lar.applicant.creditScore is oneOf(1111, 8888)
    }
}
