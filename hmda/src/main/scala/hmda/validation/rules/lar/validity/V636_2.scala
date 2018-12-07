package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._

object V636_2 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "V636-2"

  override def parent: String = "V636"

  override def apply(lar: LoanApplicationRegister): ValidationResult = {
    val applicantRace = lar.applicant.race
    val validRaceList = List(
      AmericanIndianOrAlaskaNative,
      Asian,
      BlackOrAfricanAmerican,
      NativeHawaiianOrOtherPacificIslander,
      White
    )
    val validRaceListWithEmpty = EmptyRaceValue :: validRaceList
    when(applicantRace.raceObserved is equalTo(VisualOrSurnameRace)) {
      (applicantRace.race1 is containedIn(validRaceList)) and
        (applicantRace.race2 is containedIn(validRaceListWithEmpty)) and
        (applicantRace.race3 is containedIn(validRaceListWithEmpty)) and
        (applicantRace.race4 is containedIn(validRaceListWithEmpty)) and
        (applicantRace.race5 is containedIn(validRaceListWithEmpty))
    }
  }
}
