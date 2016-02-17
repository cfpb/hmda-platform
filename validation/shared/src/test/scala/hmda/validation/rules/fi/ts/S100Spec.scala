package hmda.validation.rules.fi.ts

import org.scalatest.{ MustMatchers, PropSpec }
import org.scalatest.prop.PropertyChecks

class S100Spec extends PropSpec with PropertyChecks with MustMatchers {

//  property("Transmittal Sheet has valid activity year") {
//    forAll(tsGen) { (ts) =>
//      whenever(ts.id == 1) {
//        S100(ts, 2016)
//      }
//    }
//  }

  property("Transmittal Sheet has invalid activity year")(pending)
}

