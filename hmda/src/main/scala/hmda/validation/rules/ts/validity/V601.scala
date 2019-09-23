package hmda.validation.rules.ts.validity

import hmda.model.filing.ts.TransmittalSheet
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._

object V601 extends EditCheck[TransmittalSheet] {
  override def name: String = "V601"

  override def apply(ts: TransmittalSheet): ValidationResult =
    (ts.institutionName not empty) and
      (ts.contact.name not empty) and
      (ts.contact.email not empty) and
      (ts.contact.address.street not empty) and
      (ts.contact.address.city not empty)
}
