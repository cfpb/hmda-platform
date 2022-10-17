package hmda.validation.rules.ts.validity

import hmda.model.filing.ts.TransmittalSheet
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

object V719_2 extends EditCheck[TransmittalSheet] {
  override def name: String = "V719-2"

  override def parent: String = "V719"

  override def apply(ts: TransmittalSheet): ValidationResult = {
    ts.institutionName not equalTo(ts.LEI)
  }

}
