package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec
import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.enums._

class V636_3Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = V636_3

  property("Valid Race Must be Reported if Race not Visually Observed") {
    forAll(larGen) { lar =>
      whenever(lar.applicant.race.raceObserved != NotVisualOrSurnameRace) {
        lar.mustPass
      }
      val relevantLar = lar.copy(applicant = lar.applicant.copy(
        race = lar.applicant.race.copy(raceObserved = NotVisualOrSurnameRace)))

      relevantLar
        .copy(
          applicant = relevantLar.applicant.copy(
            race = relevantLar.applicant.race.copy(
              race1 = EmptyRaceValue,
              otherNativeRace = "",
              otherAsianRace = "",
              otherPacificIslanderRace = ""
            )))
        .mustFail

      val firstClauseTestLar = lar.copy(
        applicant = lar.applicant.copy(
          race = lar.applicant.race.copy(
            raceObserved = NotVisualOrSurnameRace,
            race2 = EmptyRaceValue,
            race3 = EmptyRaceValue,
            race4 = EmptyRaceValue,
            race5 = EmptyRaceValue
          )))

      firstClauseTestLar
        .copy(
          applicant = firstClauseTestLar.applicant.copy(
            race = firstClauseTestLar.applicant.race.copy(
              race1 = EmptyRaceValue,
              otherNativeRace = "Other"
            )))
        .mustPass

      firstClauseTestLar
        .copy(
          applicant = firstClauseTestLar.applicant.copy(
            race = firstClauseTestLar.applicant.race.copy(
              race1 = EmptyRaceValue,
              otherAsianRace = "Other"
            )))
        .mustPass

      firstClauseTestLar
        .copy(
          applicant = firstClauseTestLar.applicant.copy(
            race = firstClauseTestLar.applicant.race.copy(
              race1 = EmptyRaceValue,
              otherPacificIslanderRace = "Other"
            )))
        .mustPass

      firstClauseTestLar
        .copy(
          applicant = firstClauseTestLar.applicant.copy(
            race = firstClauseTestLar.applicant.race.copy(
              race1 = AmericanIndianOrAlaskaNative
            )))
        .mustPass

      relevantLar
        .copy(applicant = relevantLar.applicant.copy(race =
          relevantLar.applicant.race.copy(race2 = RaceInformationNotProvided)))
        .mustFail
      relevantLar
        .copy(applicant = relevantLar.applicant.copy(race =
          relevantLar.applicant.race.copy(race3 = RaceInformationNotProvided)))
        .mustFail
      relevantLar
        .copy(applicant = relevantLar.applicant.copy(race =
          relevantLar.applicant.race.copy(race4 = RaceInformationNotProvided)))
        .mustFail
      relevantLar
        .copy(applicant = relevantLar.applicant.copy(race =
          relevantLar.applicant.race.copy(race5 = RaceInformationNotProvided)))
        .mustFail

    }
  }
}
