package hmda.validation.rules.ts.validity

import hmda.model.fi.ts.TransmittalSheet
import hmda.validation.rules.EditCheck
import hmda.validation.rules.ts.TsEditCheckSpec

class V115Spec extends TsEditCheckSpec {
  property("valid when contact name not blank") {
    forAll(tsGen) { ts =>
      whenever(!ts.contact.name.isEmpty) {
        ts.mustPass
      }
    }
  }

  property("invalid when contact name blank") {
    forAll(tsGen) { ts =>
      val invalidContact = ts.contact.copy(name = "")
      val invalidTs = ts.copy(contact = invalidContact)
      invalidTs.mustFail
    }
  }

  override def check: EditCheck[TransmittalSheet] = V115
}
