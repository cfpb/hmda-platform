package hmda.validation.rules.ts.syntactical

import hmda.model.fi.ts.TransmittalSheet
import hmda.validation.rules.EditCheck
import hmda.validation.rules.ts.TsEditCheckSpec

class S028Spec extends TsEditCheckSpec {

  property("Transmittal Sheet timestamp must be numeric and in ccyymmddhhmm format") {
    forAll(tsGen) { ts =>
      whenever(ts.id == 1) {
        ts.mustPass
      }
    }
  }

  override def check: EditCheck[TransmittalSheet] = S028
}
