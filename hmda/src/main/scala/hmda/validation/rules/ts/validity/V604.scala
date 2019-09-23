package hmda.validation.rules.ts.validity

import hmda.model.census.Census
import hmda.model.filing.ts.TransmittalSheet
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

object V604 extends EditCheck[TransmittalSheet] {
  override def name: String = "V604"

  override def apply(ts: TransmittalSheet): ValidationResult =
    ts.contact.address.state is containedIn(Census.states.keys.toList)
}
