package hmda.validation.rules.lar.syntactical

import hmda.parser.fi.lar.LarGenerators
import hmda.validation.dsl.Success
import org.scalatest.prop.PropertyChecks
import org.scalatest.{ MustMatchers, PropSpec }

class S011Spec extends PropSpec with PropertyChecks with MustMatchers with LarGenerators {
  property("LAR list must not be empty") {
    forAll(larListGen) { lars =>
      S011(lars) mustBe Success()
    }
  }
}
