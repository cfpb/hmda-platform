package hmda.validation.dsl

import hmda.parser.fi.lar.LarGenerators
import hmda.validation.engine.lar.syntactical.LarSyntacticalEngine
import org.scalatest.{ MustMatchers, PropSpec }
import org.scalatest.prop.PropertyChecks

class PredicateRegExSpec
    extends PropSpec
    with PropertyChecks
    with MustMatchers
    with LarGenerators
    with LarSyntacticalEngine {

  property("A LAR must pass syntactical checks") {
    forAll(larGen) { lar =>
      whenever(lar.id == 2) {
        checkSyntactical(lar).isSuccess mustBe true
      }
    }
  }

  property("Pass syntactical checks on groups of LARs") {
    forAll(larListGen) { lars =>
      checkSyntacticalCollection(lars).isSuccess mustBe true
    }
  }

}
