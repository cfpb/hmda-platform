package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums._
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class V668_1Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = V668_1

  property("If applicant is not a person, credit score must be exempt or NA") {
    forAll(larGen) { lar =>
      val passingLar = lar.copy(
        applicant = lar.applicant
          .copy(race = lar.applicant.race.copy(race1 = new InvalidRaceCode)))
      passingLar.mustPass

      val appLar = lar.copy(
        applicant = lar.applicant.copy(
          race = lar.applicant.race.copy(race1 = RaceNotApplicable),
          ethnicity =
            lar.applicant.ethnicity.copy(ethnicity1 = EthnicityNotApplicable),
          sex = lar.applicant.sex.copy(sexEnum = SexNotApplicable)
        ))

      appLar.copy(applicant = appLar.applicant.copy(creditScore = 450)).mustFail
      appLar
        .copy(applicant = appLar.applicant.copy(creditScore = 8888))
        .mustPass
      appLar
        .copy(applicant = appLar.applicant.copy(creditScore = 1111))
        .mustPass
    }
  }
}
