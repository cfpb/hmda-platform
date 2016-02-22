package hmda.validation.rules.validity.ts

import hmda.model.fi.ts.Contact
import hmda.validation.dsl.{ Result, RegexDsl, CommonDsl }

object V155 extends CommonDsl with RegexDsl {
  def apply(contact: Contact): Result = {
    val email = contact.email
    email is validEmail
  }
}
