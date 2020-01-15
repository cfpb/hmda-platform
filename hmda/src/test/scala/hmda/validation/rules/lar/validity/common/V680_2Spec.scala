package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums._
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class V680_2Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = V680_2

  property(
    "If applicant is a non-natural person, debt-to-income must be exempt or NA") {
    forAll(larGen) { lar =>
      val unappLar = lar.copy(
        applicant = lar.applicant.copy(ethnicity =
          lar.applicant.ethnicity.copy(ethnicity1 = new InvalidEthnicityCode)))
      unappLar.mustPass

      val appLar = lar.copy(
        applicant = lar.applicant.copy(
          ethnicity =
            lar.applicant.ethnicity.copy(ethnicity1 = EthnicityNotApplicable),
          race = lar.applicant.race.copy(race1 = RaceNotApplicable),
          sex = lar.applicant.sex.copy(sexEnum = SexNotApplicable)
        ),
        coApplicant = lar.coApplicant.copy(
          ethnicity =
            lar.coApplicant.ethnicity.copy(ethnicity1 = EthnicityNotApplicable),
          race = lar.coApplicant.race.copy(race1 = RaceNotApplicable),
          sex = lar.coApplicant.sex.copy(sexEnum = SexNotApplicable)
        )
      )

      appLar.copy(loan = appLar.loan.copy(debtToIncomeRatio = "1.0")).mustFail
      appLar.copy(loan = appLar.loan.copy(debtToIncomeRatio = "test")).mustFail
      appLar.copy(loan = appLar.loan.copy(debtToIncomeRatio = "NA")).mustPass
    }
  }
}
