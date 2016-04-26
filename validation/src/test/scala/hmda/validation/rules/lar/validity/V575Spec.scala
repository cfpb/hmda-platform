package hmda.validation.rules.lar.validity

import hmda.validation.dsl.Success

class V575Spec extends RateSpreadEditCheckSpec {

  override val check = V575

  property("succeeds if lien status is 2 and rate spread is NA") {
    succeedsWhen(lienStatus = 2, rateSpread = "NA")
  }

  property("fails if lien status = 2 and rate spread is < 3.5%") {
    failsWhen(lienStatus = 2, rateSpread = "03.49")
    failsWhen(lienStatus = 2, rateSpread = "00.00")
  }

  property("succeeds if lien status = 2 and rate spread is ≥ 3.5% and ≤ 99.99%") {
    succeedsWhen(lienStatus = 2, rateSpread = "03.50")
    succeedsWhen(lienStatus = 2, rateSpread = "03.51")
    succeedsWhen(lienStatus = 2, rateSpread = "11.11")
    succeedsWhen(lienStatus = 2, rateSpread = "99.98")
    succeedsWhen(lienStatus = 2, rateSpread = "99.99")
  }

  property("works right without leading zeroes, too") {
    failsWhen(lienStatus = 2, rateSpread = "3.49")
    failsWhen(lienStatus = 2, rateSpread = "2")
    failsWhen(lienStatus = 2, rateSpread = ".05")

    succeedsWhen(lienStatus = 2, rateSpread = "3.51")
    succeedsWhen(lienStatus = 2, rateSpread = "3.5")
  }

  property("works if lien status = 2 and rate spread is otherwise badly-formatted") {
    failsWhen(lienStatus = 2, rateSpread = "-23.45")
    failsWhen(lienStatus = 2, rateSpread = "03.499")

    succeedsWhen(lienStatus = 2, rateSpread = "03.500")
    succeedsWhen(lienStatus = 2, rateSpread = "03.501")

    failsWhen(lienStatus = 2, rateSpread = "100.01")
  }

  property("fails if lien status = 2 and rate spread is utterly bogus") {
    failsWhen(lienStatus = 2, rateSpread = "OOPS")
    failsWhen(lienStatus = 2, rateSpread = "10.0w")
  }

  property("succeeds in any case when lien status is not 2") {
    forAll(larGen) { lar =>
      whenever(lar.lienStatus != 2) {
        check(lar) mustBe Success()
      }
    }
  }
}
