package hmda.validation.rules.ts.validity

import hmda.model.filing.ts.TsGenerators._
import hmda.model.filing.ts._2018.TransmittalSheet
import hmda.validation.rules.EditCheck
import hmda.validation.rules.ts.TsEditCheckSpec

class V604Spec extends TsEditCheckSpec {
  override def check: EditCheck[TransmittalSheet] = V604

  property("Contact state must be valid") {
    forAll(tsGen) { ts =>
      ts.mustPass
      ts.copy(
          contact =
            ts.contact.copy(address = ts.contact.address.copy(state = "")))
        .mustFail
    }
  }
}
