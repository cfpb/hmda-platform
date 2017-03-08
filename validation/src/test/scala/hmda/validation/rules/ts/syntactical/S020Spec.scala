package hmda.validation.rules.ts.syntactical

import hmda.validation.rules.ts.TsEditCheckSpec

class S020Spec extends TsEditCheckSpec {

  property("Transmittal Sheet Agency Code must = 1,2,3,5,7,9") {
    forAll(tsGen) { ts =>
      whenever(ts.id == 1) {
        ts.mustPass
      }
    }
  }

  property("Transmittal Sheet Agency Code must not = 8") {
    forAll(tsGen) { ts =>
      whenever(ts.id == 1) {
        val failedTs = ts.copy(agencyCode = 8)
        failedTs.mustFail
      }
    }
  }

  override def check = S020

}
