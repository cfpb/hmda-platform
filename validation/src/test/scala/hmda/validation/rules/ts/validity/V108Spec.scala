package hmda.validation.rules.ts.validity

import hmda.model.fi.ts.TransmittalSheet
import hmda.validation.rules.EditCheck
import hmda.validation.rules.ts.TsEditCheckSpec

class V108Spec extends TsEditCheckSpec {
  property("succeeds when parent and institution names are different") {
    forAll(tsGen) { ts =>
      whenever(ts.parent.name != ts.respondent.name) {
        ts.mustPass
      }
    }
  }

  property("succeeds when parent name is empty") {
    forAll(tsGen) { ts =>
      val validParent = ts.parent.copy(name = "")
      val validTs = ts.copy(parent = validParent)
      validTs.mustPass
    }
  }

  property("fails when parent and institution names are the same and not empty") {
    forAll(tsGen) { ts =>
      val name = ts.respondent.name
      whenever(!name.isEmpty) {
        val invalidParent = ts.parent.copy(name = name)
        val invalidTs = ts.copy(parent = invalidParent)
        invalidTs.mustFail
      }
    }
  }

  override def check: EditCheck[TransmittalSheet] = V108
}
