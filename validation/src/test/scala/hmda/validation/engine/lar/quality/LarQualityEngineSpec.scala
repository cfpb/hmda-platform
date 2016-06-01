package hmda.validation.engine.lar.quality

import hmda.parser.fi.lar.LarGenerators
import org.scalatest.{ MustMatchers, PropSpec }
import org.scalatest.prop.PropertyChecks

class LarQualityEngineSpec
    extends PropSpec
    with PropertyChecks
    with MustMatchers
    with LarGenerators
    with LarQualityEngine {

  property("A LAR must pass quality checks") {
    forAll(larGen) { lar =>
      whenever(lar.id == 2) {
        checkQuality(lar).isSuccess mustBe true
      }
    }
  }
}
