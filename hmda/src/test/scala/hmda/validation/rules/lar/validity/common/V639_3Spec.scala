package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec
import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.enums._

class V639_3Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = V639_3

  property("Valid Co-Race Must be Reported if Co-Race not Visually Observed") {
    forAll(larGen) { lar =>
      whenever(lar.coApplicant.race.raceObserved != NotVisualOrSurnameRace) {
        lar.mustPass
      }
      val relevantLar = lar.copy(
        coApplicant = lar.coApplicant.copy(race =
          lar.coApplicant.race.copy(raceObserved = NotVisualOrSurnameRace)))

      relevantLar
        .copy(
          coApplicant = relevantLar.coApplicant.copy(
            race = relevantLar.coApplicant.race.copy(
              race1 = EmptyRaceValue,
              otherNativeRace = "",
              otherAsianRace = "",
              otherPacificIslanderRace = ""
            )))
        .mustFail

      val firstClauseTestLar = lar.copy(
        coApplicant = lar.coApplicant.copy(
          race = lar.coApplicant.race.copy(
            raceObserved = NotVisualOrSurnameRace,
            race2 = EmptyRaceValue,
            race3 = EmptyRaceValue,
            race4 = EmptyRaceValue,
            race5 = EmptyRaceValue
          )))

      firstClauseTestLar
        .copy(
          coApplicant = firstClauseTestLar.coApplicant.copy(
            race = firstClauseTestLar.coApplicant.race.copy(
              race1 = EmptyRaceValue,
              otherNativeRace = "Other"
            )))
        .mustPass

      firstClauseTestLar
        .copy(
          coApplicant = firstClauseTestLar.coApplicant.copy(
            race = firstClauseTestLar.coApplicant.race.copy(
              race1 = EmptyRaceValue,
              otherAsianRace = "Other"
            )))
        .mustPass

      firstClauseTestLar
        .copy(
          coApplicant = firstClauseTestLar.coApplicant.copy(
            race = firstClauseTestLar.coApplicant.race.copy(
              race1 = EmptyRaceValue,
              otherPacificIslanderRace = "Other"
            )))
        .mustPass

      firstClauseTestLar
        .copy(
          coApplicant = firstClauseTestLar.coApplicant.copy(
            race = firstClauseTestLar.coApplicant.race.copy(
              race1 = AmericanIndianOrAlaskaNative
            )))
        .mustPass

      relevantLar
        .copy(
          coApplicant = relevantLar.coApplicant.copy(
            race = relevantLar.coApplicant.race.copy(
              race2 = RaceInformationNotProvided)))
        .mustFail
      relevantLar
        .copy(
          coApplicant = relevantLar.coApplicant.copy(
            race = relevantLar.coApplicant.race.copy(
              race3 = RaceInformationNotProvided)))
        .mustFail
      relevantLar
        .copy(
          coApplicant = relevantLar.coApplicant.copy(
            race = relevantLar.coApplicant.race.copy(
              race4 = RaceInformationNotProvided)))
        .mustFail
      relevantLar
        .copy(
          coApplicant = relevantLar.coApplicant.copy(
            race = relevantLar.coApplicant.race.copy(
              race5 = RaceInformationNotProvided)))
        .mustFail

    }
  }
}
