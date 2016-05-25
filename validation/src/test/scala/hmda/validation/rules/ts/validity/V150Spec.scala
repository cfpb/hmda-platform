package hmda.validation.rules.ts.validity

import hmda.model.fi.ts.TransmittalSheet
import hmda.validation.rules.EditCheck
import hmda.validation.rules.ts.TsEditCheckSpec

class V150Spec extends TsEditCheckSpec {
  property("valid when contact name and respondent name are different") {
    forAll(tsGen) { ts =>
      whenever(ts.contact.name != ts.respondent.name) {
        ts.mustPass
      }
    }
  }

  property("invalid when contact name and respondent name are the same") {
    forAll(tsGen) { ts =>
      val respondentName = ts.respondent.name
      val invalidContact = ts.contact.copy(name = respondentName)
      val invalidTs = ts.copy(contact = invalidContact)
      invalidTs.mustFail
    }
  }

  override def check: EditCheck[TransmittalSheet] = V150
}
