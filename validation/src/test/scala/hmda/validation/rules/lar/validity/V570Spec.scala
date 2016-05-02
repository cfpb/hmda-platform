package hmda.validation.rules.lar.validity

import hmda.validation.dsl.Success

class V570Spec extends RateSpreadEditCheckSpec {

  override val check = V570

  property("succeeds if lien status is 1 and rate spread is NA") {
    succeedsWhen(lienStatus = 1, rateSpread = "NA")
  }

  property("fails if lien status = 1 and rate spread is < 1.5%") {
    failsWhen(lienStatus = 1, rateSpread = "01.49")
    failsWhen(lienStatus = 1, rateSpread = "00.00")
  }

  property("succeeds if lien status = 1 and rate spread is ≥ 1.5% and ≤ 99.99%") {
    succeedsWhen(lienStatus = 1, rateSpread = "01.50")
    succeedsWhen(lienStatus = 1, rateSpread = "01.51")
    succeedsWhen(lienStatus = 1, rateSpread = "11.11")
    succeedsWhen(lienStatus = 1, rateSpread = "99.98")
    succeedsWhen(lienStatus = 1, rateSpread = "99.99")
  }

  property("fails if lien status = 1 and rate spread is > 99.99%") {
    failsWhen(lienStatus = 1, rateSpread = "100.00")
    failsWhen(lienStatus = 1, rateSpread = "999.99")
  }

  property("works right without leading zeroes, too") {
    failsWhen(lienStatus = 1, rateSpread = "1.49")
    failsWhen(lienStatus = 1, rateSpread = "1")
    failsWhen(lienStatus = 1, rateSpread = ".05")

    succeedsWhen(lienStatus = 1, rateSpread = "1.5")
    succeedsWhen(lienStatus = 1, rateSpread = "5")
  }

  property("fails if lien status = 1 and rate spread is utterly bogus") {
    failsWhen(lienStatus = 1, rateSpread = "OOPS")
    failsWhen(lienStatus = 1, rateSpread = "10.0w")
  }

  property("succeeds in any case when lien status is not 1") {
    forAll(larGen) { lar =>
      whenever(lar.lienStatus != 1) {
        check(lar) mustBe Success()
      }
    }
  }
}
