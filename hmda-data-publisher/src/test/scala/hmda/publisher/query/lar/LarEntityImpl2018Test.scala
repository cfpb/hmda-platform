package hmda.publisher.query.lar

import org.scalatest.{ FreeSpecLike, MustMatchers, PropSpec }
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import org.scalacheck.ScalacheckShapeless._
import org.scalacheck._

class LarEntityImpl2018Test extends PropSpec with ScalaCheckPropertyChecks with MustMatchers {

  property("LarEntityImpl2018 must convert to and from psv") {
    forAll { (lar: LarEntityImpl2018) =>
      val psvRow  = lar.toRegulatorPSV
      val larFromPsv = LarEntityImpl2018.parseFromPSVUnsafe(psvRow)
      larFromPsv mustBe lar
    }
  }

}