package model

import hmda.census.model.{ StateAbrv, StateAbrvLookup }
import org.scalatest.{ MustMatchers, WordSpec }

class StateAbrvLookupSpec extends WordSpec with MustMatchers {

  "State Abrv Lookup" must {
    "find states" in {
      val lookup = StateAbrvLookup.values
      val caState = lookup.find(state => state.state == "06").getOrElse(StateAbrv())
      val ilState = lookup.find(state => state.state == "17").getOrElse(StateAbrv())
      val paState = lookup.find(state => state.state == "42").getOrElse(StateAbrv())
      val prState = lookup.find(state => state.state == "72").getOrElse(StateAbrv())
      caState.stateAbrv mustBe "CA"
      ilState.stateAbrv mustBe "IL"
      paState.stateAbrv mustBe "PA"
      prState.stateAbrv mustBe "PR"
    }

    "have properly lengthed values" in {
      val lookup = StateAbrvLookup.values
      lookup.forall(state => state.stateAbrv.length == 2) mustBe true
      lookup.forall(state => state.state.length == 2) mustBe true
    }
  }

}
