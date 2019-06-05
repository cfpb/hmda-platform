package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._

object V639_2 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "V639-2"

  override def parent: String = "V639"

  override def apply(lar: LoanApplicationRegister): ValidationResult = {
    val coApplicantRace = lar.coApplicant.race
    val validRaceList = List(
      AmericanIndianOrAlaskaNative,
      Asian,
      BlackOrAfricanAmerican,
      NativeHawaiianOrOtherPacificIslander,
      White
    )
    val validRaceListWithEmpty = EmptyRaceValue :: validRaceList
    when(coApplicantRace.raceObserved is equalTo(VisualOrSurnameRace)) {
      (coApplicantRace.race1 is containedIn(validRaceList)) and
        (coApplicantRace.race2 is containedIn(validRaceListWithEmpty)) and
        (coApplicantRace.race3 is containedIn(validRaceListWithEmpty)) and
        (coApplicantRace.race4 is containedIn(validRaceListWithEmpty)) and
        (coApplicantRace.race5 is containedIn(validRaceListWithEmpty))
    }
  }
}
