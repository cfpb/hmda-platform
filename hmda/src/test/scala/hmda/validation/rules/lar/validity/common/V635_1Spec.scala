package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums._
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class V635_1Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = V635_1

  property("If other races are blank, race 1 must have a value") {
    forAll(larGen) { lar =>
      val applicableLar = lar.copy(
        applicant = lar.applicant.copy(
          race = lar.applicant.race.copy(otherNativeRace = "",
                                         otherAsianRace = "",
                                         otherPacificIslanderRace = "")))

      val unapplicableLar = lar.copy(
        applicant = lar.applicant.copy(
          race = lar.applicant.race.copy(otherNativeRace = "test")))
      unapplicableLar.mustPass

      val raceValid = applicableLar.applicant.race
        .copy(race1 = AmericanIndianOrAlaskaNative)
      val raceInvalid = applicableLar.applicant.race
        .copy(race1 = new InvalidRaceCode)
      lar
        .copy(applicant = applicableLar.applicant.copy(race = raceValid))
        .mustPass
      lar
        .copy(applicant = applicableLar.applicant.copy(race = raceInvalid))
        .mustFail
    }
  }
}
