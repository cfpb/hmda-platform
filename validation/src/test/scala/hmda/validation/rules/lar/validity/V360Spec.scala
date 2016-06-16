package hmda.validation.rules.lar.validity

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec
import org.scalacheck.Gen

class V360Spec extends LarEditCheckSpec {
  val goodOptionGen: Gen[List[String]] = Gen.listOfN(3, Gen.alphaStr)

  property("Denials with all different values must pass") {
    forAll(larGen, goodOptionGen) { (lar: LoanApplicationRegister, goodOptions: List[String]) =>
      val goodOptionList = goodOptions.filterNot(_.isEmpty)
      whenever(goodOptionList.distinct.length == goodOptionList.length) {
        val goodDenial = lar.denial.copy(reason1 = goodOptions(0), reason2 = goodOptions(1), reason3 = goodOptions(2))
        val newLar = lar.copy(denial = goodDenial)
        newLar.mustPass
      }
    }
  }

  property("Denials with two empty strings must pass") {
    forAll(larGen) { lar =>
      val goodDenial = lar.denial.copy(reason3 = "", reason1 = "")
      val newLar = lar.copy(denial = goodDenial)
      newLar.mustPass
    }
  }

  val badOptionGen: Gen[Int] = Gen.choose(Int.MinValue, Int.MaxValue)

  property("Denials with the two of the same value must fail") {
    forAll(larGen, badOptionGen) { (lar: LoanApplicationRegister, x: Int) =>
      val badDenial = lar.denial.copy(reason1 = x.toString, reason2 = x.toString)
      val newLar = lar.copy(denial = badDenial)
      newLar.mustFail
    }
  }

  override def check: EditCheck[LoanApplicationRegister] = V360
}
