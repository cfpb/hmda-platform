package hmda.publisher.query.lar

import org.scalatest.{ MustMatchers, PropSpec }
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import org.scalacheck.ScalacheckShapeless._

class LarEntityImpl2020Test extends PropSpec with ScalaCheckPropertyChecks with MustMatchers {

  property("LarEntityImpl2020 must convert to and from psv") {
    forAll { (lar: LarEntityImpl2020) =>
      val psvRow  = lar.toRegulatorPSV
      val larFromPsv = LarEntityImpl2020.parseFromPSVUnsafe(psvRow)
      larFromPsv mustBe lar.copy(larPartSeven = LarPartSeven2020())
    }
  }

}