package hmda.validation.rules.lar.validity

import hmda.validation.rules.lar.LarEditCheckSpec

// this may become a trait, but for now it's in the hierarchy
abstract class RateSpreadEditCheckSpec extends LarEditCheckSpec {

  def failsWhen(lienStatus: Int, rateSpread: String): Any = {
    forAll(larGen) { lar =>
      val newLar = lar.copy(lienStatus = lienStatus, rateSpread = rateSpread)
      newLar.mustFail
    }
  }

  def failsWhen(rateSpread: String): Any = {
    forAll(larGen) { lar =>
      val newLar = lar.copy(rateSpread = rateSpread)
      newLar.mustFail
    }
  }

  def succeedsWhen(lienStatus: Int, rateSpread: String): Any = {
    forAll(larGen) { lar =>
      val newLar = lar.copy(lienStatus = lienStatus, rateSpread = rateSpread)
      newLar.mustPass
    }
  }

  def succeedsWhen(rateSpread: String): Any = {
    forAll(larGen) { lar =>
      val newLar = lar.copy(rateSpread = rateSpread)
      newLar.mustPass
    }
  }
}
