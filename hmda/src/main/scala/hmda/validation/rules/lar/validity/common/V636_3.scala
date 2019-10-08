package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._

object V636_3 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "V636-3"

  override def parent: String = "V636"

  override def apply(lar: LoanApplicationRegister): ValidationResult = {
    val applicantRace = lar.applicant.race
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

    when(applicantRace.raceObserved is equalTo(NotVisualOrSurnameRace)) {
      (applicantRace.race1 is containedIn(validRace1List) or
        ((applicantRace.race1 is equalTo(EmptyRaceValue)) and (applicantRace.otherNativeRace not empty)) or
        ((applicantRace.race1 is equalTo(EmptyRaceValue)) and (applicantRace.otherAsianRace not empty)) or
        ((applicantRace.race1 is equalTo(EmptyRaceValue)) and (applicantRace.otherPacificIslanderRace not empty))) and
        (applicantRace.race2 is containedIn(validAllOtherRaceList)) and
        (applicantRace.race3 is containedIn(validAllOtherRaceList)) and
        (applicantRace.race4 is containedIn(validAllOtherRaceList)) and
        (applicantRace.race5 is containedIn(validAllOtherRaceList))
    }
  }
}
