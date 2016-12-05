package hmda.validation.rules.ts.validity

import hmda.model.fi.ts.TransmittalSheet
import hmda.validation.dsl.Result
import hmda.validation.rules.EditCheck
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.PredicateRegEx._

object V155 extends EditCheck[TransmittalSheet] {

  override def apply(ts: TransmittalSheet): Result = {
    val contact = ts.contact
    val email = contact.email
    email is validEmail
  }

  override def name: String = "V155"

  override def description = ""
}
