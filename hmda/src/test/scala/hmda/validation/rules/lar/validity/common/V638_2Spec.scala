package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums._
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class V638_2Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = V638_2

  property("Races 2-5 must contain valid values") {
    forAll(larGen) { lar =>
      val validRace =
        lar.coApplicant.race.copy(race2 = EmptyRaceValue,
                                  race3 = EmptyRaceValue,
                                  race4 = EmptyRaceValue,
                                  race5 = EmptyRaceValue)

      val invalidRace =
        lar.coApplicant.race.copy(race2 = EmptyRaceValue,
                                  race3 = RaceNotApplicable,
                                  race4 = new InvalidRaceCode,
                                  race5 = EmptyRaceValue)

      val validLar =
        lar.copy(coApplicant = lar.coApplicant.copy(race = validRace))
      validLar.mustPass

      val invalidLar =
        lar.copy(coApplicant = lar.coApplicant.copy(race = invalidRace))
      invalidLar.mustFail
    }
  }
}
