package hmda.validation.rules.lar.validity

import hmda.parser.fi.lar.LarGenerators
import hmda.validation.dsl.{ Failure, Success }
import org.scalatest.prop.PropertyChecks
import org.scalatest.{ MustMatchers, PropSpec }

class V375Spec extends PropSpec with PropertyChecks with MustMatchers with LarGenerators {

  property("if purchaser type is 2, then loan type 2, 3, or 4 must succeed") {
    forAll(larGen) { lar =>
      val newLar = lar.copy(purchaserType = 2)
      whenever(List(2, 3, 4).contains(newLar.loan.loanType)) {
        V375(newLar) mustBe Success()
      }
    }
  }

  property("if purchaser type is 2, then loan type other than 2, 3, or 4 must fail") {
    forAll(larGen) { lar =>
      val newLoan = lar.loan.copy(loanType = 1)
      val newLar = lar.copy(purchaserType = 2, loan = newLoan)
      V375(newLar) mustBe a[Failure]
    }
  }

  property("if purchaser type is not 2, then any loan type succeeds") {
    forAll(larGen) { lar =>
      whenever(lar.purchaserType != 2) {
        V375(lar) mustBe Success()
      }
    }
  }

}
