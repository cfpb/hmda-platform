package hmda.validation.engine.lar.syntactical

import hmda.model.fi.lar.LarGenerators
import hmda.validation.context.ValidationContext
import org.scalatest.{ MustMatchers, PropSpec }
import org.scalatest.prop.PropertyChecks

import scalaz.Success

class LarSyntacticalEngineSpec
    extends PropSpec
    with PropertyChecks
    with MustMatchers
    with LarGenerators
    with LarSyntacticalEngine {

  property("A LAR must pass syntactical checks") {
    forAll(larGen) { lar =>
      whenever(lar.id == 2) {
        checkSyntactical(lar, ValidationContext(None, None)) mustBe a[Success[_]]
      }
    }
  }

  property("Pass syntactical checks on groups of LARs") {
    forAll(larListGen) { lars =>
      checkSyntacticalCollection(lars) mustBe a[Success[_]]
    }
  }

}
