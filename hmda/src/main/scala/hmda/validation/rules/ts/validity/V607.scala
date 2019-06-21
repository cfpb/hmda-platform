package hmda.validation.rules.ts.validity

import hmda.model.filing.ts._2018.TransmittalSheet
import hmda.validation.dsl.PredicateRegEx._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

object V607 extends EditCheck[TransmittalSheet] {
  override def name: String = "V607"

  override def apply(ts: TransmittalSheet): ValidationResult = {
    ts.taxId is validTaxId
  }
}
