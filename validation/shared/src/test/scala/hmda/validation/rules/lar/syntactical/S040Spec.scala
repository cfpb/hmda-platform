package hmda.validation.rules.lar.syntactical

import hmda.parser.fi.lar.LarGenerators
import hmda.validation.dsl.Success
import org.scalatest.prop.PropertyChecks
import org.scalatest.{ MustMatchers, PropSpec }

class S040Spec extends PropSpec with PropertyChecks with MustMatchers with LarGenerators {
  property("Loan/Application number must be unique") {
    forAll(larListGen) { lars =>
      S040(lars) mustBe Success()
    }
  }
}
