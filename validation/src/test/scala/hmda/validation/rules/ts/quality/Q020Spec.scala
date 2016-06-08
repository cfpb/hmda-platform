package hmda.validation.rules.ts.quality

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.model.fi.ts.TransmittalSheet
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.BadValueUtils
import hmda.validation.rules.ts.TsEditCheckSpec
import org.scalacheck.Gen

class Q020Spec extends TsEditCheckSpec {
  property("Valid if parent and institution address not the same") {
    forAll(tsGen) { (ts) =>
      whenever(ts.respondent.address != ts.parent.address) {
        ts.mustPass
      }
    }
  }

  property("Invalid if parent and institution address are the same") {
    forAll(tsGen) { (ts) =>
      val newParent = ts.parent.copy(address = ts.respondent.address)
      val newTs = ts.copy(parent = newParent)
      newTs.mustFail
    }
  }

  override def check: EditCheck[TransmittalSheet] = Q020
}
