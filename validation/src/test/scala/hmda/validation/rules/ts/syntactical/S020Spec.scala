package hmda.validation.rules.ts.syntactical

import hmda.model.fi.ts.TransmittalSheet
import hmda.validation.rules.EditCheck
import hmda.validation.rules.ts.TsEditCheckSpec

class S020Spec extends TsEditCheckSpec {

  property("Transmittal Sheet Agency Code must = 1,2,3,5,7,9") {
    forAll(tsGen) { ts =>
      whenever(ts.id == 1) {
        ts.mustPass
      }
    }
  }

  override def check: EditCheck[TransmittalSheet] = S020

}
