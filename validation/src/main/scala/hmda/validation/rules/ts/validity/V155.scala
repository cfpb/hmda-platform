package hmda.validation.rules.ts.validity

import hmda.model.fi.ts.TransmittalSheet
import hmda.validation.dsl.{ RegexDsl, Result }
import hmda.validation.rules.EditCheck

object V155 extends EditCheck[TransmittalSheet] with RegexDsl {
  def apply(ts: TransmittalSheet): Result = {
    val contact = ts.contact
    val email = contact.email
    email is validEmail
  }

  override def name: String = "V155"
}
