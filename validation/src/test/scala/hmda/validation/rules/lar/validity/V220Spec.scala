package hmda.validation.rules.lar.validity

import hmda.model.fi.lar.{ Loan, LoanApplicationRegister }
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.{ BadValueUtils, LarEditCheckSpec }
import org.scalacheck.Gen

class V220Spec extends LarEditCheckSpec with BadValueUtils {
  property("Loan Type must = 1,2,3, or 4") {
    forAll(larGen) { lar =>
      whenever(lar.id == 2) {
        lar.mustPass
      }
    }
  }

  val badLoanTypeGen: Gen[Int] = intOutsideRange(1, 4)

  property("Loan Type other than 1,2,3,4 is invalid") {
    forAll(larGen, badLoanTypeGen) { (lar: LoanApplicationRegister, x: Int) =>
      val invalidLoan: Loan = lar.loan.copy(loanType = x)
      val invalidLar = lar.copy(loan = invalidLoan)
      invalidLar.mustFail
    }
  }

  override def check: EditCheck[LoanApplicationRegister] = V220
}
