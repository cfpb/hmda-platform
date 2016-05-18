package hmda.validation.rules.lar.validity

import hmda.model.fi.lar.{ LoanApplicationRegister, Loan }
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec
import org.scalacheck.Gen

class V250Spec extends LarEditCheckSpec with BadValueUtils {
  property("Income must be numeric and positive") {
    forAll(larGen) { lar =>
      lar.mustPass
    }
  }

  val invalidLoanAmount = Gen.choose(Integer.MIN_VALUE, 0)

  property("Invalid if income <= 0") {
    forAll(larGen, invalidLoanAmount) { (lar: LoanApplicationRegister, x: Int) =>
      val invalidLoan: Loan = lar.loan.copy(amount = x)
      val invalidLar: LoanApplicationRegister = lar.copy(loan = invalidLoan)
      invalidLar.mustFail
    }
  }

  override def check: EditCheck[LoanApplicationRegister] = V250
}