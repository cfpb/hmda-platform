package hmda.validation.rules.lar.syntactical

import hmda.model.filing.ts.TransmittalLar
import hmda.validation.dsl.PredicateCommon.equalTo
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck
import hmda.validation.dsl.PredicateSyntax._

object S307 extends EditCheck[TransmittalLar] {
  override def name: String = "S307"
  override def apply(tsLar: TransmittalLar): ValidationResult =
    tsLar.allActionTakenDatesWithinQuarter is equalTo(true)
}
