package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec
import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.enums.InvalidRaceObservedCode

class V639_1Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = V639_1

  property("Co-Applicant Observed Race Code Must be Valid") {
    forAll(larGen) { lar =>
      whenever(lar.coApplicant.race.raceObserved != new InvalidRaceObservedCode) {
        lar.mustPass
      }
      lar
        .copy(
          coApplicant = lar.coApplicant.copy(race =
            lar.coApplicant.race.copy(raceObserved = new InvalidRaceObservedCode)))
        .mustFail

    }
  }
}
