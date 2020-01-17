package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums._
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class V638_1Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = V638_1

  property("If other races are blank, race 1 must have a value") {
    forAll(larGen) { lar =>
      val applicableLar = lar.copy(
        coApplicant = lar.coApplicant.copy(
          race = lar.coApplicant.race.copy(otherNativeRace = "",
                                           otherAsianRace = "",
                                           otherPacificIslanderRace = "")))

      val unapplicableLar = lar.copy(
        coApplicant = lar.coApplicant.copy(
          race = lar.coApplicant.race.copy(otherNativeRace = "test")))
      unapplicableLar.mustPass

      val raceValid = applicableLar.coApplicant.race
        .copy(race1 = AmericanIndianOrAlaskaNative)
      val raceInvalid = applicableLar.coApplicant.race
        .copy(race1 = new InvalidRaceCode)
      lar
        .copy(coApplicant = applicableLar.coApplicant.copy(race = raceValid))
        .mustPass
      lar
        .copy(coApplicant = applicableLar.coApplicant.copy(race = raceInvalid))
        .mustFail
    }
  }
}
