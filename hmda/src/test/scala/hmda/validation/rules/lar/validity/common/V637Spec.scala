package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums._
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class V637Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = V637

  property("If race is not applicable, race observed must be not applicable") {
    forAll(larGen) { lar =>
      val applicableLar = lar.copy(
        applicant = lar.applicant.copy(
          race = lar.applicant.race.copy(race1 = RaceNotApplicable)))

      val unapplicableLar = lar.copy(
        applicant = lar.applicant.copy(
          race = lar.applicant.race.copy(race1 = new InvalidRaceCode)))
      unapplicableLar.mustPass

      val raceNA = applicableLar.applicant.race
        .copy(raceObserved = RaceObservedNotApplicable)
      val raceInvalid = applicableLar.applicant.race
        .copy(raceObserved = new InvalidRaceObservedCode)
      lar
        .copy(applicant = applicableLar.applicant.copy(race = raceNA))
        .mustPass
      lar
        .copy(applicant = applicableLar.applicant.copy(race = raceInvalid))
        .mustFail
    }
  }
}
