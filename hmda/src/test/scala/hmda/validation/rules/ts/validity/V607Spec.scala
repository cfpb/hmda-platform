package hmda.validation.rules.ts.validity

import hmda.model.filing.ts.TsGenerators._
import hmda.model.filing.ts._2018.TransmittalSheet
import hmda.validation.rules.EditCheck
import hmda.validation.rules.ts.TsEditCheckSpec

class V607Spec extends TsEditCheckSpec {
  override def check: EditCheck[TransmittalSheet] = V607

  property("Tax ID must be a valid format") {
    forAll(tsGen) { ts =>
      ts.mustPass
      ts.copy(taxId = "").mustFail
    }
  }
}
