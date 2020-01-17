package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums.InvalidRaceObservedCode
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._

object V639_1 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "V639-1"

  override def parent: String = "V639"

  override def apply(lar: LoanApplicationRegister): ValidationResult =
    lar.coApplicant.race.raceObserved not equalTo(new InvalidRaceObservedCode)
}
