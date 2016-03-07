package hmda.validation.rules.ts.syntactical

import hmda.parser.fi.ts.TsGenerators
import hmda.validation.dsl.Success
import org.scalatest.prop.PropertyChecks
import org.scalatest.{ PropSpec, MustMatchers }

class S010Spec extends PropSpec with MustMatchers with PropertyChecks with TsGenerators {

  property("Record identifier must be 1") {
    forAll(tsGen) { ts =>
      whenever(ts.id == 1) {
        S010(ts) mustBe Success()
      }
    }
  }

}
