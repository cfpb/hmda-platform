package hmda.validation.rules.lar.validity

import hmda.validation.dsl.Success

class V520Spec extends RateSpreadEditCheckSpec {
  override def check = V520

  property("succeeds if lien status = 3 and rate spread is NA") {
    succeedsWhen(lienStatus = 3, rateSpread = "NA")
  }

  property("fails if lien status = 3 and rate spread is not NA") {
    failsWhen(lienStatus = 3, rateSpread = "00.00")
    failsWhen(lienStatus = 3, rateSpread = "02.50")
    failsWhen(lienStatus = 3, rateSpread = "3.51")
    failsWhen(lienStatus = 3, rateSpread = "whatever")
  }

  property("succeeds in any case if lien status != 3") {
    forAll(larGen) { lar =>
      whenever(lar.lienStatus != 3) {
        lar.mustPass
      }
    }
  }
}
