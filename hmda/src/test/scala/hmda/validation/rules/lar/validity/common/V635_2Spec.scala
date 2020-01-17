package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums._
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class V635_2Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = V635_2

  property("Races 2-5 must contain valid values") {
    forAll(larGen) { lar =>
      val validRace =
        lar.applicant.race.copy(race2 = EmptyRaceValue,
                                race3 = EmptyRaceValue,
                                race4 = EmptyRaceValue,
                                race5 = EmptyRaceValue)

      val invalidRace =
        lar.applicant.race.copy(race2 = EmptyRaceValue,
                                race3 = RaceNotApplicable,
                                race4 = new InvalidRaceCode,
                                race5 = EmptyRaceValue)

      val validLar =
        lar.copy(applicant = lar.applicant.copy(race = validRace))
      validLar.mustPass

      val invalidLar =
        lar.copy(applicant = lar.applicant.copy(race = invalidRace))
      invalidLar.mustFail
    }
  }
}
