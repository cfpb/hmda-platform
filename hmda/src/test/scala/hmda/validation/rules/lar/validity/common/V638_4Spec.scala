package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums._
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class V638_4Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = V638_4

  property("If race 1 is not available, race 2-5 must be blank") {
    forAll(larGen) { lar =>
      val applicableApplicant = lar.coApplicant.copy(
        race = lar.coApplicant.race.copy(race1 = RaceNotApplicable))

      val unapplicableLar = lar.copy(
        coApplicant = lar.coApplicant.copy(
          race = lar.coApplicant.race.copy(race1 = new InvalidRaceCode)))
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
        .copy(coApplicant = applicableApplicant.copy(race = raceBlank))
        .mustPass
      lar
        .copy(coApplicant = applicableApplicant.copy(race = raceNotBlank))
        .mustFail
    }
  }
}
