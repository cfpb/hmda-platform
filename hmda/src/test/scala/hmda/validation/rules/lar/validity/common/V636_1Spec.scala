package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec
import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.enums.{InvalidRaceObservedCode, RaceObservedNoCoApplicant}

class V636_1Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = V636_1

  property("Applicant Observed Race Code Must be Valid") {
    forAll(larGen) { lar =>
      whenever(lar.applicant.race.raceObserved != new InvalidRaceObservedCode && lar.applicant.race.raceObserved != RaceObservedNoCoApplicant) {
        lar.mustPass
      }
      lar
        .copy(
          applicant = lar.applicant.copy(race =
            lar.applicant.race.copy(raceObserved = new InvalidRaceObservedCode)))
        .mustFail

    }
  }
}
