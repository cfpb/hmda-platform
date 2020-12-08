package hmda.validation.rules.ts.validity

import hmda.model.filing.ts.TransmittalSheet
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

object V719 extends EditCheck[TransmittalSheet] {
  override def name: String = "V719"

  override def apply(ts: TransmittalSheet): ValidationResult = {
    ts.institutionName not numeric
  }

}
