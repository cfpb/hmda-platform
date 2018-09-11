package hmda.validation.rules.ts.syntactical

import hmda.model.filing.ts.TransmittalSheet
import hmda.model.filing.ts.TsGenerators._
import hmda.validation.context.ValidationContext
import hmda.validation.rules.EditCheck
import hmda.validation.rules.ts.TsEditCheckSpec

class S302Spec extends TsEditCheckSpec {
  override def check: EditCheck[TransmittalSheet] =
    S302.withContext(ValidationContext())

  property("Pass when year is reported correctly (2018)") {
    forAll(tsGen) { ts =>
      whenever(ts.year == 2018) {
        ts.mustPass
      }
    }
  }

  property("Fail when year is reported incorrectly") {
    forAll(tsGen) { ts =>
      whenever(ts.year != 2018) {
        ts.mustFail
      }
    }
  }

}
