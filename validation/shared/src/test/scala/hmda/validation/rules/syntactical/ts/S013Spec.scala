package hmda.validation.rules.syntactical.ts

import hmda.parser.fi.ts.TsGenerators
import hmda.validation.dsl.Success
import org.scalatest.{ MustMatchers, PropSpec }
import org.scalatest.prop.PropertyChecks

class S013Spec extends PropSpec with PropertyChecks with MustMatchers with TsGenerators {

  property("timestamp must be later than timestamp in database") {
    forAll(tsGen) { ts =>
      val checkTimestamp = 201301111330L
      whenever(ts.id == 1) {
        S013(ts, checkTimestamp) mustBe Success()
      }
    }
  }

}
