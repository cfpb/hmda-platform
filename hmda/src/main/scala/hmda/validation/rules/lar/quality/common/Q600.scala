package hmda.validation.rules.lar.quality.common

import hmda.model.filing.ts.TransmittalLar
import hmda.validation.dsl.PredicateCommon.equalTo
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck
import hmda.validation.dsl.PredicateSyntax._

object Q600 extends EditCheck[TransmittalLar] {
  override def name: String = "Q600"
  override def apply(tsLar: TransmittalLar): ValidationResult =
    tsLar.ts.totalLines is equalTo(tsLar.distinctUliCount.toInt)
}
