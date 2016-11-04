package model

import org.scalatest.{ MustMatchers, WordSpec }

class PrPopLookupSpec extends WordSpec with MustMatchers {

  "Pr Pop Lookup" must {
    "find pr small county values" in {
      val lookup = PrPopLookup.values
      val large = lookup.find(pop => pop.key == "72097").getOrElse(StatesPopulation())
      val small = lookup.find(pop => pop.key == "72083").getOrElse(StatesPopulation())
      large.smallCounty mustBe "0"
      small.smallCounty mustBe "1"
    }

    "have a key of 5 characters" in {
      val lookup = PrPopLookup.values
      lookup.forall(pop => pop.key.length == 5) mustBe true
    }
  }

}
