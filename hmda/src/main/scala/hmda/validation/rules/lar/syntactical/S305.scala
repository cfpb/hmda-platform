package hmda.validation.rules.lar.syntactical

import hmda.model.filing.ts.TransmittalLar
import hmda.validation.dsl.PredicateCommon.equalTo
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck
import hmda.validation.dsl.PredicateSyntax._

object S305 extends EditCheck[TransmittalLar] {
  override def name: String = "S305"
  override def apply(tsLar: TransmittalLar): ValidationResult =
    tsLar.larsCount is equalTo(tsLar.larsDistinctCount.toInt)

}
