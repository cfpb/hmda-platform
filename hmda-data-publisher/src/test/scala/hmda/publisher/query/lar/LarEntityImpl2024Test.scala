package hmda.publisher.query.lar

import org.scalacheck.ScalacheckShapeless._
import org.scalatest.{MustMatchers, PropSpec}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

class LarEntityImpl2024Test extends PropSpec with ScalaCheckPropertyChecks with MustMatchers {

  property("LarEntityImpl2024 must convert to and from psv") {
    forAll { (lar: LarEntityImpl2024) =>
      val psvRow  = lar.toRegulatorPSV
      val larFromPsv = LarEntityImpl2024.parseFromPSVUnsafe(psvRow)
      larFromPsv mustBe lar.copy(larPartSeven = LarPartSeven2024())
    }
  }

}