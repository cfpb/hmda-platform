package hmda.validation.rules.ts.validity

import hmda.model.filing.ts.TransmittalSheet
import hmda.model.filing.ts.TsGenerators._
import hmda.validation.rules.EditCheck
import hmda.validation.rules.ts.TsEditCheckSpec

class V717Spec extends TsEditCheckSpec {
  override def check: EditCheck[TransmittalSheet] = V717

  property("Email must be a valid format") {
    forAll(tsGen) { ts =>
      ts.mustPass
      ts.copy(contact = ts.contact.copy(email = "")).mustFail
      ts.copy(contact = ts.contact.copy(email = "abcdef")).mustFail
      ts.copy(contact = ts.contact.copy(email = "abcdef@abc")).mustFail
      ts.copy(contact = ts.contact.copy(email = "abcdef.abc")).mustFail
    }
  }
}
