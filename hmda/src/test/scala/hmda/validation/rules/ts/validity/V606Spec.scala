package hmda.validation.rules.ts.validity

import hmda.model.filing.ts.TransmittalSheet
import hmda.model.filing.ts.TsGenerators._
import hmda.validation.rules.EditCheck
import hmda.validation.rules.ts.TsEditCheckSpec

class V606Spec extends TsEditCheckSpec {
  override def check: EditCheck[TransmittalSheet] = V606

  property("Total lines must be greater than 0") {
    forAll(tsGen) { ts =>
      ts.mustPass
      ts.copy(totalLines = 0).mustFail
    }
  }
}
