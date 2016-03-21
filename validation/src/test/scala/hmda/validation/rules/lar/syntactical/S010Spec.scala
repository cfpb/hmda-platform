package hmda.validation.rules.lar.syntactical

import hmda.parser.fi.lar.LarGenerators
import hmda.validation.dsl.Success
import org.scalatest.{ MustMatchers, PropSpec }
import org.scalatest.prop.PropertyChecks

class S010Spec extends PropSpec with PropertyChecks with MustMatchers with LarGenerators {

  property("Record identifier must be 2") {
    forAll(larGen) { lar =>
      whenever(lar.id == 2) {
        S010(lar) mustBe Success()
      }
    }
  }
}
