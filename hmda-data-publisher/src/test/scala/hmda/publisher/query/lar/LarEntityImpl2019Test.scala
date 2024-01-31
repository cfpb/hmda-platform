package hmda.publisher.query.lar

import hmda.model.publication.Msa
import org.scalatest.{ MustMatchers, PropSpec }
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import org.scalacheck.ScalacheckShapeless._

class LarEntityImpl2019Test extends PropSpec with ScalaCheckPropertyChecks with MustMatchers {

  property("LarEntityImpl2019 must convert to and from psv") {
    forAll { (lar: LarEntityImpl2019) =>
      val psvRow     = lar.toRegulatorPSV
      val larFromPsv = LarEntityImpl2019.parseFromPSVUnsafe(psvRow)
      larFromPsv mustBe lar.copy(larPartSeven = LarPartSeven2019())
    }
  }

  property("LarEntityImpl2019WithMsa must convert to and from psv") {
    forAll { (lar: LarEntityImpl2019WithMsa) =>
      // csv uses only some columns of part seven
      val partSeven = {
        val empty = LarPartSeven2019()
        lar.larEntityImpl2019.larPartSeven.copy(
          ethnicityCategorization = empty.ethnicityCategorization,
          raceCategorization = empty.raceCategorization,
          sexCategorization = empty.sexCategorization,
          dwellingCategorization = empty.dwellingCategorization,
          loanProductTypeCategorization = empty.loanProductTypeCategorization
        )
      }
      val psvRow     = lar.toRegulatorPSV
      val larFromPsv = LarEntityImpl2019WithMsa.parseFromPSVUnsafe(psvRow)
      larFromPsv mustBe lar.copy(
        msa = Msa(lar.msa.id, lar.msa.name),// csv uses only those 2 columns, rest will be default
        larEntityImpl2019 = lar.larEntityImpl2019.copy(larPartSeven = partSeven)
      )
    }
  }

}