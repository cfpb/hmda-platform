package hmda.validation.rules.ts.validity

import hmda.model.fi.ts.TransmittalSheet
import hmda.validation.rules.ts.TsEditCheckSpec
import hmda.validation.rules.EditCheck

class V140Spec extends TsEditCheckSpec with ValidityUtils {

  property("Respondent state code must equal a valid postal code abbreviation") {
    forAll(tsGen) { ts =>
      val r = ts.respondent
      whenever(respondentNotEmpty(r)) {
        ts.mustPass
      }
    }
  }

  property("Wrong or missing respondent state code should fail") {
    forAll(tsGen) { ts =>
      val r1 = ts.respondent.copy(state = "")
      val badTs1 = ts.copy(respondent = r1)
      val r2 = ts.respondent.copy(state = "XXX")
      val badTs2 = ts.copy(respondent = r2)
      whenever(ts.id == 1) {
        badTs1.mustFail
        badTs2.mustFail
      }
    }
  }

  override def check: EditCheck[TransmittalSheet] = V140
}
