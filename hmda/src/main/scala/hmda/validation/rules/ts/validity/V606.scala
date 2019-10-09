package hmda.validation.rules.ts.validity

import hmda.model.filing.ts.TransmittalSheet
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

object V606 extends EditCheck[TransmittalSheet] {
  override def name: String = "V606"

  override def apply(ts: TransmittalSheet): ValidationResult =
    ts.totalLines is greaterThan(0)
}
