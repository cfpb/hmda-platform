package hmda.validation.rules.lar.validity

import hmda.validation.dsl.{ Failure, Success }
import hmda.validation.rules.lar.LarEditCheckSpec

class V262Spec extends LarEditCheckSpec {
  property("If date application received = NA, then action taken type must = 6") {
    forAll(larGen) { lar =>
      whenever(lar.id == 2) {
        val naLoan = lar.loan.copy(applicationDate = "NA")
        val v262Lar = lar.copy(loan = naLoan, actionTakenType = 6)
        V262(v262Lar) mustBe Success()
      }
    }
  }

  property("Must fail if date application received = NA and action taken type is not 6") {
    forAll(larGen) { lar =>
      whenever(lar.actionTakenType != 6) {
        val naLoan = lar.loan.copy(applicationDate = "NA")
        val v262Lar = lar.copy(loan = naLoan)
        V262(v262Lar) mustBe a[Failure]
      }
    }
  }

  property("If date application received != NA, V262 should pass") {
    forAll(larGen, dateGen) { (lar, altDate) =>
      val datedLoan = lar.loan.copy(applicationDate = altDate.toString)
      val datedLar = lar.copy(loan = datedLoan)
      V262(datedLar) mustBe Success()
    }
  }
}
