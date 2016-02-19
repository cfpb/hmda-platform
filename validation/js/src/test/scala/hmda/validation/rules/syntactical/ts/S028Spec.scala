package hmda.validation.rules.syntactical.ts

import hmda.parser.fi.ts.TsGenerators
import hmda.validation.dsl.Success
import org.scalacheck.Gen
import org.scalatest.prop.PropertyChecks
import org.scalatest.{ MustMatchers, PropSpec }

class S028Spec extends PropSpec with PropertyChecks with MustMatchers with TsGenerators {

  property("Transmittal Sheet timestamp must be numeric and in ccyymmddhhmm format") {
    forAll(tsGen) { ts =>
      whenever(ts.id == 1) {
        S028(ts) mustBe Success()
      }
    }
  }

}
