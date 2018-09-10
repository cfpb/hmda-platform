package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec
import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.enums._

class V636_2Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = V636_2

  property("Applicant Race must Be Generic if Observed") {
    forAll(larGen) { lar =>
      whenever(lar.applicant.race.raceObserved != VisualOrSurnameRace) {
        lar.mustPass
      }
      val relevantLar = lar.copy(
        applicant = lar.applicant.copy(
          race = lar.applicant.race.copy(raceObserved = VisualOrSurnameRace)))

      relevantLar
        .copy(
          applicant = relevantLar.applicant.copy(
            race = relevantLar.applicant.race.copy(race1 = EmptyRaceValue)))
        .mustFail
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

      relevantLar
        .copy(
          applicant = relevantLar.applicant.copy(
            race = relevantLar.applicant.race.copy(
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
