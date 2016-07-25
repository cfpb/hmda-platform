package hmda.validation.engine.lar.syntactical

import hmda.parser.fi.lar.LarGenerators
import org.scalatest.{ MustMatchers, PropSpec }
import org.scalatest.prop.PropertyChecks

class LarSyntacticalEngineSpec
    extends PropSpec
    with PropertyChecks
    with MustMatchers
    with LarGenerators
    with LarSyntacticalEngine {

  property("A LAR must pass syntactical checks") {
    forAll(larGen) { lar =>
      whenever(lar.id == 2) {
        checkSyntactical(lar, None).isSuccess mustBe true
      }
    }
  }

  property("Pass syntactical checks on groups of LARs") {
    forAll(larListGen) { lars =>
      checkSyntacticalCollection(lars).isSuccess mustBe true
    }
  }

}
