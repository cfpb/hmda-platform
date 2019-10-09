package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums._
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

object V680_1 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "V680-1"

  override def parent: String = "V680"

  override def apply(lar: LoanApplicationRegister): ValidationResult =
    when(
      lar.applicant.ethnicity.ethnicity1 is equalTo(EthnicityNotApplicable) and
        (lar.applicant.race.race1 is equalTo(RaceNotApplicable)) and
        (lar.applicant.sex.sexEnum is equalTo(SexNotApplicable)) and
        (lar.coApplicant.ethnicity.ethnicity1 is equalTo(EthnicityNoCoApplicant)) and
        (lar.coApplicant.race.race1 is equalTo(RaceNoCoApplicant)) and
        (lar.coApplicant.sex.sexEnum is equalTo(SexNoCoApplicant))
    ) {

      lar.loan.debtToIncomeRatio is oneOf("NA", "Exempt")
    }
}
