package hmda.validation.rules.lar.validity

import hmda.parser.fi.lar.LarGenerators
import hmda.validation.dsl.{ Failure, Success }
import org.scalatest.prop.PropertyChecks
import org.scalatest.{ MustMatchers, PropSpec }

class V375Spec extends PropSpec with PropertyChecks with MustMatchers with LarGenerators {

  property("if purchaser type is 2, then loan type must be 2, 3, or 4") {
    forAll(larGen) { lar =>
      val newLar = lar.copy(purchaserType = 2)
      if (List(2, 3, 4).contains(newLar.loan.loanType)) {
        V375(newLar) mustBe Success()
      } else {
        V375(newLar) mustBe a[Failure]
      }
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
