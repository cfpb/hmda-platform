package hmda.validation.rules.ts.validity

import hmda.model.filing.ts.TransmittalSheet
import hmda.validation.rules.EditCheck
import hmda.validation.rules.ts.TsEditCheckSpec
import hmda.model.filing.ts.TsGenerators._

class V602Spec extends TsEditCheckSpec {
  override def check: EditCheck[TransmittalSheet] = V602

  property("Calendar quarter must be 4") {
    forAll(tsGen) { ts =>
      ts.mustPass
      ts.copy(quarter = 2).mustFail
    }
  }
}
