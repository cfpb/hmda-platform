package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._

object V639_3 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "V639-3"

  override def parent: String = "V639"

  override def apply(lar: LoanApplicationRegister): ValidationResult = {
    val coApplicantRace = lar.coApplicant.race
    val validRaceBaseList = List(
      AmericanIndianOrAlaskaNative,
      Asian,
      AsianIndian,
      Chinese,
      Filipino,
      Japanese,
      Korean,
      Vietnamese,
      OtherAsian,
      BlackOrAfricanAmerican,
      NativeHawaiianOrOtherPacificIslander,
      NativeHawaiian,
      GuamanianOrChamorro,
      Samoan,
      OtherPacificIslander,
      White
    )
    val validRace1List        = RaceInformationNotProvided :: validRaceBaseList
    val validAllOtherRaceList = EmptyRaceValue :: validRaceBaseList

    when(coApplicantRace.raceObserved is equalTo(NotVisualOrSurnameRace)) {
      (coApplicantRace.race1 is containedIn(validRace1List) or
        ((coApplicantRace.race1 is equalTo(EmptyRaceValue)) and (coApplicantRace.otherNativeRace not empty)) or
        ((coApplicantRace.race1 is equalTo(EmptyRaceValue)) and (coApplicantRace.otherAsianRace not empty)) or
        ((coApplicantRace.race1 is equalTo(EmptyRaceValue)) and (coApplicantRace.otherPacificIslanderRace not empty))) and
        (coApplicantRace.race2 is containedIn(validAllOtherRaceList)) and
        (coApplicantRace.race3 is containedIn(validAllOtherRaceList)) and
        (coApplicantRace.race4 is containedIn(validAllOtherRaceList)) and
        (coApplicantRace.race5 is containedIn(validAllOtherRaceList))
    }
  }
}
