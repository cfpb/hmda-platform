package hmda.validation.rules.lar.validity

class V500Spec extends RateSpreadEditCheckSpec {
  override def check = V500

  property("succeeds if rate spread is NA") {
    succeedsWhen(rateSpread = "NA")
  }

  property("fails if rate spread is blank") {
    failsWhen(rateSpread = "")
  }

  property("succeeds if rate spread has NN.NN format") {
    succeedsWhen(rateSpread = "00.00")
    succeedsWhen(rateSpread = "03.50")
    succeedsWhen(rateSpread = "99.99")
  }

  property("fails if rate spread has other numeric format") {
    failsWhen(rateSpread = "00.000")
    failsWhen(rateSpread = "0.000")
    failsWhen(rateSpread = "000.0")
    failsWhen(rateSpread = "1.50")
    failsWhen(rateSpread = ".22")
    failsWhen(rateSpread = "01.")
  }

  property("fails if rate spread is not numeric") {
    failsWhen(rateSpread = "other")
  }
}
