package hmda.publisher.query.lar

import org.scalacheck.ScalacheckShapeless._
import org.scalatest.{MustMatchers, PropSpec}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

class LarEntityImpl2022Test extends PropSpec with ScalaCheckPropertyChecks with MustMatchers {

  property("LarEntityImpl2022 must convert to and from psv") {
    forAll { (lar: LarEntityImpl2022) =>
      val psvRow  = lar.toRegulatorPSV
      val larFromPsv = LarEntityImpl2022.parseFromPSVUnsafe(psvRow)
      larFromPsv mustBe lar.copy(larPartSeven = LarPartSeven2022())
    }
  }

}