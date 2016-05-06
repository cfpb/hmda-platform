package hmda.validation.rules.lar.validity

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.dsl.{ Failure, Success }
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

// this may become a trait, but for now it's in the hierarchy
abstract class RateSpreadEditCheckSpec extends LarEditCheckSpec {
  def check: EditCheck[LoanApplicationRegister]

  def failsWhen(lienStatus: Int, rateSpread: String): Any = {
    forAll(larGen) { lar =>
      val newLar = lar.copy(lienStatus = lienStatus, rateSpread = rateSpread)
      check(newLar) mustBe a[Failure]
    }
  }

  def failsWhen(rateSpread: String): Any = {
    forAll(larGen) { lar =>
      val newLar = lar.copy(rateSpread = rateSpread)
      check(newLar) mustBe a[Failure]
    }
  }

  def succeedsWhen(lienStatus: Int, rateSpread: String): Any = {
    forAll(larGen) { lar =>
      val newLar = lar.copy(lienStatus = lienStatus, rateSpread = rateSpread)
      check(newLar) mustBe a[Success]
    }
  }

  def succeedsWhen(rateSpread: String): Any = {
    forAll(larGen) { lar =>
      val newLar = lar.copy(rateSpread = rateSpread)
      check(newLar) mustBe a[Success]
    }
  }
}
