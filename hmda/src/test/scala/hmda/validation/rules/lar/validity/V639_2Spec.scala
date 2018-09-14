package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec
import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.enums._

class V639_2Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = V639_2

  property("Co-Applicant Race must Be Generic if Observed") {
    forAll(larGen) { lar =>
      whenever(lar.coApplicant.race.raceObserved != VisualOrSurnameRace) {
        lar.mustPass
      }
      val relevantLar = lar.copy(
        coApplicant = lar.coApplicant.copy(
          race = lar.coApplicant.race.copy(raceObserved = VisualOrSurnameRace)))

      relevantLar
        .copy(
          coApplicant = relevantLar.coApplicant.copy(
            race = relevantLar.coApplicant.race.copy(race1 = EmptyRaceValue)))
        .mustFail
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

      relevantLar
        .copy(
          coApplicant = relevantLar.coApplicant.copy(
            race = relevantLar.coApplicant.race.copy(
              race1 = AmericanIndianOrAlaskaNative,
              race2 = EmptyRaceValue,
              race3 = EmptyRaceValue,
              race4 = EmptyRaceValue,
              race5 = EmptyRaceValue
            )))
        .mustPass

    }
  }
}
