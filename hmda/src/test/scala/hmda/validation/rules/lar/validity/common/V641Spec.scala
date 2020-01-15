package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums._
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class V641Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = V641

  property("If race is no co-applicant, race observed must be no co-applicant") {
    forAll(larGen) { lar =>
      val applicableLar = lar.copy(
        coApplicant = lar.coApplicant.copy(
          race = lar.coApplicant.race.copy(race1 = RaceNoCoApplicant,
                                           raceObserved =
                                             RaceObservedNoCoApplicant)))
      applicableLar.mustPass

      val unapplicableLar = lar.copy(
        coApplicant = lar.coApplicant.copy(
          race = lar.coApplicant.race.copy(race1 = new InvalidRaceCode,
                                           raceObserved =
                                             new InvalidRaceObservedCode)))
      unapplicableLar.mustPass

      val raceO = applicableLar.coApplicant.race
        .copy(raceObserved = RaceObservedNotApplicable)
      val raceEmpty = applicableLar.coApplicant.race
        .copy(race1 = EmptyRaceValue)
      lar
        .copy(coApplicant = applicableLar.coApplicant.copy(race = raceO))
        .mustFail
      lar
        .copy(coApplicant = applicableLar.coApplicant.copy(race = raceEmpty))
        .mustFail
    }
  }
}
