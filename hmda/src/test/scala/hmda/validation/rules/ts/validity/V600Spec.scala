package hmda.validation.rules.ts.validity

import hmda.model.filing.ts.TransmittalSheet
import hmda.validation.rules.EditCheck
import hmda.validation.rules.ts.TsEditCheckSpec
import hmda.model.filing.ts.TsGenerators._

class V600Spec extends TsEditCheckSpec {
  override def check: EditCheck[TransmittalSheet] = V600

  property("LEI in TS must be 20 characters long") {
    forAll(tsGen) { ts =>
      whenever(ts.LEI != "") {
        ts.mustPass
      }
      ts.copy(LEI = "").mustFail
      ts.copy(LEI = "ABCD").mustFail
    }
  }
}
