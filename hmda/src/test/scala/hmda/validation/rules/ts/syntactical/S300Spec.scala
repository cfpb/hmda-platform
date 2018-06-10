package hmda.validation.rules.ts.syntactical

import hmda.model.filing.ts.TransmittalSheet
import hmda.validation.rules.EditCheck
import hmda.validation.rules.ts.TsEditCheckSpec
import hmda.model.filing.ts.TsGenerators._

class S300Spec extends TsEditCheckSpec {
  override def check: EditCheck[TransmittalSheet] = S300

  property("TS record identifier must be 1") {
    forAll(tsGen) { ts =>
      whenever(ts.id == 1) {
        ts.mustPass
      }
    }
  }
}
