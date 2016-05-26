package hmda.validation.rules.ts.validity

import hmda.model.fi.ts.TransmittalSheet
import hmda.validation.rules.EditCheck
import hmda.validation.rules.ts.TsEditCheckSpec

class V111Spec extends TsEditCheckSpec with ValidityUtils {

  property("Parent state code must equal a valid state code abbreviation") {
    forAll(tsGen) { ts =>
      whenever(!ts.parent.state.isEmpty) {
        ts.mustPass
      }
    }
  }

  property("Empty state code should pass") {
    forAll(tsGen) { ts =>
      val s1 = ts.parent.copy(state = "")
      val goodTs1 = ts.copy(parent = s1)
      goodTs1.mustPass
    }
  }

  property("Invalid state code should fail") {
    forAll(tsGen) { ts =>
      val s2 = ts.parent.copy(state = "XXX")
      val badTs2 = ts.copy(parent = s2)
      badTs2.mustFail
    }
  }

  override def check: EditCheck[TransmittalSheet] = V111
}
