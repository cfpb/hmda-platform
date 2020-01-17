package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums._
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class V635_4Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = V635_4

  property("If race 1 is not available, race 2-5 must be blank") {
    forAll(larGen) { lar =>
      val applicableApplicant = lar.applicant.copy(
        race = lar.applicant.race.copy(race1 = RaceNotApplicable))

      val unapplicableLar = lar.copy(
        applicant = lar.applicant.copy(
          race = lar.applicant.race.copy(race1 = new InvalidRaceCode)))
      unapplicableLar.mustPass

      val raceBlank =
        applicableApplicant.race.copy(race2 = EmptyRaceValue,
                                      race3 = EmptyRaceValue,
                                      race4 = EmptyRaceValue,
                                      race5 = EmptyRaceValue)

      val raceNotBlank =
        applicableApplicant.race.copy(race2 = EmptyRaceValue,
                                      race3 = new InvalidRaceCode,
                                      race4 = EmptyRaceValue,
                                      race5 = new InvalidRaceCode)

      lar
        .copy(applicant = applicableApplicant.copy(race = raceBlank))
        .mustPass
      lar
        .copy(applicant = applicableApplicant.copy(race = raceNotBlank))
        .mustFail
    }
  }
}
