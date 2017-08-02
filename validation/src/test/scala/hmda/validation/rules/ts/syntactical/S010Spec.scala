package hmda.validation.rules.ts.syntactical

import hmda.model.fi.ts.TransmittalSheet
import hmda.validation.rules.EditCheck
import hmda.validation.rules.ts.TsEditCheckSpec

class S010Spec extends TsEditCheckSpec {

  property("Record identifier must be 1") {
    forAll(tsGen) { ts =>
      whenever(ts.id == 1) {
        ts.mustPass
      }
    }
  }

  override def check: EditCheck[TransmittalSheet] = S010
}
