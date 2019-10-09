package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums.{ NotVisualOrSurnameRace, RaceObservedNotApplicable, VisualOrSurnameRace }
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._

object V636_1 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "V636-1"

  override def parent: String = "V636"

  val validRaceObserved =
    List(VisualOrSurnameRace, NotVisualOrSurnameRace, RaceObservedNotApplicable)

  override def apply(lar: LoanApplicationRegister): ValidationResult =
    lar.applicant.race.raceObserved is containedIn(validRaceObserved)
}
