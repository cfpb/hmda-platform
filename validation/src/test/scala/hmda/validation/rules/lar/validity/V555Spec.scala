package hmda.validation.rules.lar.validity

import hmda.model.fi.lar.{ Loan, LoanApplicationRegister }
import hmda.validation.dsl.{ Failure, Success }
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.{ BadValueUtils, LarEditCheckSpec }
import org.scalacheck.Gen

class V555Spec extends LarEditCheckSpec with BadValueUtils {

  val validLoanPurposeGen: Gen[Int] = Gen.choose(Int.MinValue, Int.MaxValue).filter(!List(1, 3).contains(_))

  property("Valid when loan purpose != 1 or 3") {
    forAll(larGen, validLoanPurposeGen) { (lar, x) =>
      val validLoan = lar.loan.copy(purpose = x)
      val validLar = lar.copy(loan = validLoan)
      validLar.mustPass
    }
  }

  val validLienStatusGen: Gen[Int] = Gen.oneOf(1, 2, 4)

  property("Valid when lien status = 1, 2, or 4") {
    forAll(larGen, validLienStatusGen) { (lar, x) =>
      val validLar = lar.copy(lienStatus = x)
      validLar.mustPass
    }
  }

  val invalidLienStatusGen: Gen[Int] = Gen.choose(Int.MinValue, Int.MaxValue).filter(!List(1, 2, 4).contains(_))
  val invalidLoanPurposeGen: Gen[Int] = Gen.oneOf(1, 3)

  property("Invalid when loan purpose is 1 or 3 and lien status not 1,2, or 3") {
    forAll(larGen, invalidLienStatusGen, invalidLoanPurposeGen) {
      (lar: LoanApplicationRegister, ls: Int, lp: Int) =>
        val invalidLoan = lar.loan.copy(purpose = lp)
        val invalidLar: LoanApplicationRegister = lar.copy(lienStatus = ls, loan = invalidLoan)
        invalidLar.mustFail
    }
  }

  override def check: EditCheck[LoanApplicationRegister] = V555
}
