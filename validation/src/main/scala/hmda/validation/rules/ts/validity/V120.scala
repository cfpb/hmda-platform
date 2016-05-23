package hmda.validation.rules.ts.validity

import hmda.model.fi.ts.TransmittalSheet
import hmda.validation.dsl.Result
import hmda.validation.rules.EditCheck
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.PredicateRegEx._

object V120 extends EditCheck[TransmittalSheet] {

  override def apply(ts: TransmittalSheet): Result = {
    ts.contact.phone is validPhoneNumber
  }

  override def name = "V120"
}
