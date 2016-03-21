package hmda.validation.rules.ts.validity

import hmda.model.fi.ts.Contact
import hmda.validation.dsl.{ CommonDsl, RegexDsl, Result }

object V155 extends CommonDsl with RegexDsl {
  def apply(contact: Contact): Result = {
    val email = contact.email
    email is validEmail
  }
}
