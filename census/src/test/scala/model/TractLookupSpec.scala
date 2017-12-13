package model

import hmda.census.model.TractLookup
import hmda.model.fi.lar.{ Geography, LarGenerators }
import org.scalatest.{ MustMatchers, WordSpec }

class TractLookupSpec extends WordSpec with MustMatchers with LarGenerators {

  "Tract Lookup" must {
    val lookup = TractLookup.values

    val geo1 = Geography(state = "01", county = "001", tract = "020100", msa = "any")
    val geo2 = Geography(state = "06", county = "113", tract = "010800", msa = "any")

    val lar1 = larGen.sample.get.copy(geography = geo1)
    val lar2 = larGen.sample.get.copy(geography = geo2)

    "have properly lengthed values" in {
      lookup.forall(tract => tract.state.length == 2) mustBe true
      lookup.forall(tract => tract.county.length == 3) mustBe true
      lookup.forall(tract => tract.key.length == 5) mustBe true
      lookup.forall(tract => tract.tract.length == 6) mustBe true
      lookup.forall(tract => tract.tractDec.length == 7) mustBe true
    }

    "have the correct number of tracts" in {
      lookup.size mustBe 75883
    }

    "given a LAR, return the Tract for its State + County + Tract combination" in {
      TractLookup.forLar(lar1).isDefined mustBe true
      TractLookup.forLar(lar2).isDefined mustBe true
    }

    "given a LAR with invalid geography, return None" in {
      val invalidGeo = Geography("xx", "yy", "zz", "ww")
      TractLookup.forLar(larGen.sample.get.copy(geography = invalidGeo)).isEmpty mustBe true
    }

    "have minority population percentage" in {
      TractLookup.forLar(lar1).get.minorityPopulationPercent mustBe 12.58
      TractLookup.forLar(lar2).get.minorityPopulationPercent mustBe 64.35
    }

    "have realistic values for minority population percentage" in {
      lookup.forall(_.minorityPopulationPercent >= 0) mustBe true
      lookup.forall(_.minorityPopulationPercent <= 100) mustBe true
    }

    "have tract MFI (Median Family Income) percentage of MSA MFI" in {
      TractLookup.forLar(lar1).get.tractMfiPercentageOfMsaMfi mustBe 122.93
      TractLookup.forLar(lar2).get.tractMfiPercentageOfMsaMfi mustBe 70.39
    }

    "have realistic values for MFI comparison" in {
      lookup.forall(_.tractMfiPercentageOfMsaMfi >= 0) mustBe true
      lookup.forall(_.tractMfiPercentageOfMsaMfi <= 1000) mustBe true
    }
  }
}
