package hmda.validation.rules.lar.quality

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec
import org.scalacheck.Gen

class Q666Spec extends LarEditCheckSpec {

  property("fails check when loan ID only contains alpha characters") {
    val invalidLoanId = Gen.alphaStr.sample.getOrElse("AAA")
    forAll(larGen) { lar =>
      val invalidLoan = lar.loan.copy(id = invalidLoanId)
      val invalidLar = lar.copy(loan = invalidLoan)
      invalidLar.mustFail
    }
  }

  val punctuation = Gen.oneOf('#', '(', ')', '.', '-', ' ')
  val alphaPunctuationStr = stringOfOneToN(25, Gen.oneOf(Gen.alphaChar, punctuation))
  property("fails check when loan ID only contains alpha and punctuation characters") {
    val invalidLoanId = Gen.alphaStr.sample.getOrElse("AAA!")
    forAll(larGen) { lar =>
      val invalidLoan = lar.loan.copy(id = invalidLoanId)
      val invalidLar = lar.copy(loan = invalidLoan)
      invalidLar.mustFail
    }
  }

  property("pass when some elements of loan id are numeric") {
    forAll(larGen, alphaPunctuationStr) { (lar, alphaLoanId) =>
      val validLoan = lar.loan.copy(id = alphaLoanId + "3")
      val validLar = lar.copy(loan = validLoan)
      validLar.mustPass
    }
  }

  property("fails when loanID is formatted like a SSN") {
    val first = Gen.choose(100, 999)
    val second = Gen.choose(10, 99)
    val third = Gen.choose(1000, 9999)

    forAll(larGen, first, second, third) { (lar, d1, d2, d3) =>
      val ssn = s"$d1-$d2-$d3"
      val invalidLoan = lar.loan.copy(id = ssn)
      val invalidLar = lar.copy(loan = invalidLoan)
      invalidLar.mustFail
    }
  }

  override def check: EditCheck[LoanApplicationRegister] = Q666
}
