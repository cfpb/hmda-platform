package hmda.validation.rules.ts.validity

import hmda.model.filing.ts.TransmittalSheet
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._

object V600 extends EditCheck[TransmittalSheet] {
  override def name: String = "V600"

  override def apply(ts: TransmittalSheet): ValidationResult =
    ts.LEI.length is equalTo(20)

}
