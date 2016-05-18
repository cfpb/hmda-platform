package hmda.validation.rules.lar.validity

import hmda.model.fi.lar.{ Denial, LoanApplicationRegister }
import hmda.validation.dsl.{ Failure, Success }
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec
import org.scalacheck.Gen

class V360Spec extends LarEditCheckSpec {
  property("Denials with all different values must pass") {
    forAll(larGen) { lar =>
      val goodDenial = lar.denial.copy(reason1 = "1", reason2 = "2", reason3 = "")
      val newLar = lar.copy(denial = goodDenial)
      newLar.mustPass
    }
  }

  property("Denials with two empty strings must pass") {
    forAll(larGen) { lar =>
      val goodDenial = lar.denial.copy(reason3 = "", reason1 = "")
      val newLar = lar.copy(denial = goodDenial)
      newLar.mustPass
    }
  }

  val badOptionGen: Gen[Int] = Gen.choose(1, 9)

  property("Denials with the two of the same value must fail") {
    forAll(larGen, badOptionGen) { (lar: LoanApplicationRegister, x: Int) =>
      val badDenial = lar.denial.copy(reason1 = x.toString, reason2 = x.toString)
      val newLar = lar.copy(denial = badDenial)
      newLar.mustFail
    }
  }

  override def check: EditCheck[LoanApplicationRegister] = V360
}
