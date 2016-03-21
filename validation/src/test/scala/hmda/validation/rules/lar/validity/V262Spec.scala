package hmda.validation.rules.lar.validity

import hmda.parser.fi.lar.LarGenerators
import hmda.validation.dsl.Success
import org.scalacheck.Gen
import org.scalatest.{ MustMatchers, PropSpec }
import org.scalatest.prop.PropertyChecks

class V262Spec extends PropSpec with PropertyChecks with MustMatchers with LarGenerators {
  property("If date application received = NA, then action taken type must = 6") {
    forAll(larGen) { lar =>
      whenever(lar.id == 2) {
        val naLoan = lar.loan.copy(applicationDate = "NA")
        val v262Lar = lar.copy(loan = naLoan, actionTakenType = 6)
        V262(v262Lar) mustBe Success()
      }
    }
  }

  property("If date application received != NA, V262 should pass") {
    forAll(larGen) { lar =>
      whenever(lar.id == 2 && lar.loan.applicationDate != "NA") {
        V262(lar) mustBe Success()
      }
    }
  }
}
