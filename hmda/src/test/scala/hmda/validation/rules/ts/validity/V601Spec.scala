package hmda.validation.rules.ts.validity

import hmda.model.filing.ts.TransmittalSheet
import hmda.validation.rules.EditCheck
import hmda.validation.rules.ts.TsEditCheckSpec
import hmda.model.filing.ts.TsGenerators._

class V601Spec extends TsEditCheckSpec {
  override def check: EditCheck[TransmittalSheet] = V601

  property("Contact fields must not be blank") {
    forAll(tsGen) { ts =>
      whenever(
        ts.contact.name != "" &&
          ts.contact.email != "" &&
          ts.contact.address.street != "" &&
          ts.contact.address.city != "" &&
          ts.institutionName != "") {
        ts.mustPass
      }
      ts.copy(institutionName = "").mustFail
      ts.copy(contact = ts.contact.copy(email = "")).mustFail
      ts.copy(contact = ts.contact.copy(name = "")).mustFail
      ts.copy(
          contact =
            ts.contact.copy(address = ts.contact.address.copy(street = "")))
        .mustFail

      ts.copy(
          contact =
            ts.contact.copy(address = ts.contact.address.copy(city = "")))
        .mustFail
    }
  }
}
