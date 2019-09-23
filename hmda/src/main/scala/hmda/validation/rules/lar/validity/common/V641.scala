package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums._
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

object V641 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "V641"

  override def apply(lar: LoanApplicationRegister): ValidationResult =
    when(lar.coApplicant.race.race1 is equalTo(RaceNoCoApplicant)) {
      lar.coApplicant.race.raceObserved is equalTo(RaceObservedNoCoApplicant)
    } and when(lar.coApplicant.race.raceObserved is equalTo(RaceObservedNoCoApplicant)) {
      lar.coApplicant.race.race1 is equalTo(RaceNoCoApplicant)
    }
}
