package hmda.validation.rules.ts.validity

import hmda.model.filing.ts.TransmittalSheet
import hmda.model.filing.ts.TsGenerators._
import hmda.validation.rules.EditCheck
import hmda.validation.rules.ts.TsEditCheckSpec

class V605Spec extends TsEditCheckSpec {
  override def check: EditCheck[TransmittalSheet] = V605

  property("Zip code must be valid") {
    forAll(tsGen) { ts =>
      ts.mustPass
      ts.copy(
          contact =
            ts.contact.copy(address = ts.contact.address.copy(zipCode = "")))
        .mustFail
    }
  }
}
