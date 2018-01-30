package hmda.validation.rules.lar.quality

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec
import org.scalacheck.Gen

class Q666Spec extends LarEditCheckSpec {

  property("fails check when all elements of loan id are characters") {
    val invalidLoanId = Gen.alphaStr.sample.getOrElse("AAA")
    forAll(larGen) { lar =>
      val invalidLoan = lar.loan.copy(id = invalidLoanId)
      val invalidLar = lar.copy(loan = invalidLoan)
      invalidLar.mustFail
    }
  }

  property("pass when some elements of loan id are numeric") {
    val validLoanId = Gen.alphaNumStr.sample.getOrElse("AAA3")
    forAll(larGen) { lar =>
      val validLoan = lar.loan.copy(id = validLoanId)
      val validLar = lar.copy(loan = validLoan)
      validLar.mustPass
    }
  }

  override def check: EditCheck[LoanApplicationRegister] = Q666
}
