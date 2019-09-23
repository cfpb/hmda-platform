package hmda.validation.rules.ts.validity

import hmda.model.filing.ts.TransmittalSheet
import hmda.validation.dsl.PredicateRegEx._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

object V603 extends EditCheck[TransmittalSheet] {
  override def name: String = "V603"

  override def apply(ts: TransmittalSheet): ValidationResult =
    ts.contact.phone is validPhoneNumber
}
