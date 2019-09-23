package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums._
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

object V640 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "V640"

  override def apply(lar: LoanApplicationRegister): ValidationResult =
    when(lar.coApplicant.race.race1 is equalTo(RaceNotApplicable)) {
      lar.coApplicant.race.raceObserved is equalTo(RaceObservedNotApplicable)
    }
}
