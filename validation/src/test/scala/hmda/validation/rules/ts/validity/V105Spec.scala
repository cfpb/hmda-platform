package hmda.validation.rules.ts.validity

import hmda.model.fi.ts.TransmittalSheet
import hmda.validation.rules.ts.TsEditCheckSpec
import hmda.validation.rules.EditCheck

class V105Spec extends TsEditCheckSpec with ValidityUtils {

  property("Respondent name, address, city, state and zip code must not be blank") {
    forAll(tsGen) { ts =>
      val r = ts.respondent
      whenever(respondentNotEmpty(r)) {
        ts.mustPass
      }
    }
  }

  override def check: EditCheck[TransmittalSheet] = V105
}
