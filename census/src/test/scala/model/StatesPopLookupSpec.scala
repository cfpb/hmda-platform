package model

import hmda.census.model.{ StatesPopLookup, StatesPopulation }
import org.scalatest.{ MustMatchers, WordSpec }

class StatesPopLookupSpec extends WordSpec with MustMatchers {

  "State Pop Lookup" must {

    "return correct small county values" in {
      val lookup = StatesPopLookup.values
      val largeOne = lookup.find(pop => pop.key == "06073").getOrElse(StatesPopulation())
      val smallOne = lookup.find(pop => pop.key == "48301").getOrElse(StatesPopulation())
      val largeTwo = lookup.find(pop => pop.key == "51059").getOrElse(StatesPopulation())
      val smallTwo = lookup.find(pop => pop.key == "31115").getOrElse(StatesPopulation())
      largeOne.smallCounty mustBe "0"
      smallOne.smallCounty mustBe "1"
      largeTwo.smallCounty mustBe "0"
      smallTwo.smallCounty mustBe "1"
    }

    "have a key of 5 characters" in {
      val lookup = StatesPopLookup.values
      lookup.forall(pop => pop.key.length == 5) mustBe true
    }
  }

}
