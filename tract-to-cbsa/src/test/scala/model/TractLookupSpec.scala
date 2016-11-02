package model

import org.scalatest.{ MustMatchers, WordSpec }

class TractLookupSpec extends WordSpec with MustMatchers {

  "Tract Lookup" must {

    "have properly lengthed values" in {
      val lookup = TractLookup.values
      lookup.forall(tract => tract.state.length == 2) mustBe true
      lookup.forall(tract => tract.county.length == 3) mustBe true
      lookup.forall(tract => tract.key.length == 5) mustBe true
      lookup.forall(tract => tract.tract.length == 6) mustBe true
      lookup.forall(tract => tract.tractDec.length == 7) mustBe true
    }

    "have the correct number of tracts" in {
      val lookup = TractLookup.values
      lookup.size mustBe 74002
    }
  }
}
