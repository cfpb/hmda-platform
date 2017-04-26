package model

import hmda.census.model.{ Cbsa, CbsaLookup }
import org.scalatest.{ MustMatchers, WordSpec }

class CbsaLookupSpec extends WordSpec with MustMatchers {

  "Cbsa Lookup" must {
    "find cbsas" in {
      val lookup = CbsaLookup.values
      val cincinnati = lookup.find(cbsa => cbsa.key == "18029").getOrElse(Cbsa())
      val vernal = lookup.find(cbsa => cbsa.key == "49047").getOrElse(Cbsa())
      val emptyOne = lookup.find(cbsa => cbsa.key == "37039").getOrElse(Cbsa())
      val emptyTwo = lookup.find(cbsa => cbsa.key == "48301").getOrElse(Cbsa())
      cincinnati.cbsa mustBe "17140"
      vernal.cbsa mustBe "46860"
      emptyOne mustBe Cbsa()
      emptyTwo mustBe Cbsa()
    }

    "match a cbsa with its name" in {
      CbsaLookup.nameFor("46860") mustBe "Vernal, UT"
      CbsaLookup.nameFor("17140") mustBe "Cincinnati, OH-KY-IN"
      CbsaLookup.nameFor("wrongId") mustBe "NA"
    }
  }

}
