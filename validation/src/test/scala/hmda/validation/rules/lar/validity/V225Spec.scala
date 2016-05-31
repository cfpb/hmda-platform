package hmda.validation.rules.lar.validity

import hmda.model.fi.lar.{ Loan, LoanApplicationRegister }
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec
import org.scalacheck.Gen

class V225Spec extends LarEditCheckSpec with BadValueUtils {
  property("Loan Purpose must = 1, 2, or 3") {
    forAll(larGen) { lar =>
      lar.mustPass
    }
  }

  val badLoanPurposeGen: Gen[Int] = intOutsideRange(1, 3)

  property("Loan Purpose other than 1,2,3 is invalid") {
    forAll(larGen, badLoanPurposeGen) { (lar: LoanApplicationRegister, x: Int) =>
      val invalidLoan: Loan = lar.loan.copy(purpose = x)
      val invalidLar: LoanApplicationRegister = lar.copy(loan = invalidLoan)
      invalidLar.mustFail
    }
  }

  override def check: EditCheck[LoanApplicationRegister] = V225
}
