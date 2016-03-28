package hmda.validation.rules.ts.validity

import hmda.model.fi.ts.{ Contact, TransmittalSheet }
import hmda.validation.dsl.{ CommonDsl, RegexDsl, Result }

object V155 extends CommonDsl with RegexDsl {
  def apply(ts: TransmittalSheet): Result = {
    val contact = ts.contact
    val email = contact.email
    email is validEmail
  }
}
