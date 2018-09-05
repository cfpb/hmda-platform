package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums._
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class V652_2Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = V652_2

  property("If co-applicant does not exist, co-applicant age must be 8888") {
    forAll(larGen) { lar =>
      val unapplicableLar = lar.copy(
        coApplicant = lar.coApplicant.copy(
          race = lar.coApplicant.race.copy(race1 = InvalidRaceCode)))
      unapplicableLar.mustPass

      val ethnicityNA =
        lar.coApplicant.ethnicity.copy(ethnicity1 = EthnicityNotApplicable)
      val raceNA = lar.coApplicant.race.copy(race1 = RaceNotApplicable)
      val sexNA = lar.coApplicant.sex.copy(sexEnum = SexNotApplicable)
      val applicableLar = lar.copy(
        coApplicant = lar.coApplicant
          .copy(race = raceNA, ethnicity = ethnicityNA, sex = sexNA))
      applicableLar.mustFail

      val validLar =
        applicableLar.copy(
          coApplicant = applicableLar.coApplicant.copy(age = 8888))
      validLar.mustPass
    }
  }
}
