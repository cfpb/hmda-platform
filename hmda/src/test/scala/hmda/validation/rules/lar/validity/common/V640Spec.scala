package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums._
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class V640Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = V640

  property("If race is not applicable, race observed must not be applicable") {
    forAll(larGen) { lar =>
      val applicableLar = lar.copy(
        coApplicant = lar.coApplicant.copy(
          race = lar.coApplicant.race.copy(race1 = RaceNotApplicable)))

      val unapplicableLar = lar.copy(
        coApplicant = lar.coApplicant.copy(
          race = lar.coApplicant.race.copy(race1 = new InvalidRaceCode)))
      unapplicableLar.mustPass

      val raceNA = applicableLar.coApplicant.race
        .copy(raceObserved = RaceObservedNotApplicable)
      val raceVis = applicableLar.coApplicant.race
        .copy(raceObserved = new InvalidRaceObservedCode)
      lar
        .copy(coApplicant = applicableLar.coApplicant.copy(race = raceNA))
        .mustPass
      lar
        .copy(coApplicant = applicableLar.coApplicant.copy(race = raceVis))
        .mustFail
    }
  }
}
