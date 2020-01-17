package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums._
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class V668_2Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = V668_2

  property("If co-applicant is not a person, credit score must be exempt or NA") {
    forAll(larGen) { lar =>
      val passingLar = lar.copy(
        coApplicant = lar.coApplicant
          .copy(race = lar.coApplicant.race.copy(race1 = new InvalidRaceCode)))
      passingLar.mustPass

      val appLar = lar.copy(
        coApplicant = lar.coApplicant.copy(
          race = lar.coApplicant.race.copy(race1 = RaceNotApplicable),
          ethnicity =
            lar.coApplicant.ethnicity.copy(ethnicity1 = EthnicityNotApplicable),
          sex = lar.coApplicant.sex.copy(sexEnum = SexNotApplicable)
        ))

      appLar
        .copy(coApplicant = appLar.coApplicant.copy(creditScore = 450))
        .mustFail
      appLar
        .copy(coApplicant = lar.coApplicant.copy(creditScore = 8888))
        .mustPass
      appLar
        .copy(coApplicant = lar.coApplicant.copy(creditScore = 1111))
        .mustPass
    }
  }
}
