package hmda.validation.rules.ts.validity

import hmda.model.filing.ts.TransmittalSheet
import hmda.model.filing.ts.TsGenerators._
import hmda.validation.rules.EditCheck
import hmda.validation.rules.ts.TsEditCheckSpec

class V719Spec extends TsEditCheckSpec {
  override def check: EditCheck[TransmittalSheet] = V719

  property("Institution Name Must Not Be Only Numeric") {
    forAll(tsGen) { ts =>
      ts.copy(institutionName = "123").mustFail
      ts.copy(institutionName = "abcdef123").mustPass
      ts.copy(institutionName = "bank 0").mustPass
    }
  }
}
