package hmda.validation.rules.lar.validity

import hmda.model.fi.lar.{ Loan, LoanApplicationRegister }
import hmda.validation.dsl.{ Failure, Success }
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec
import org.scalacheck.Gen

class V220Spec extends LarEditCheckSpec with BadValueUtils {
  property("Loan Type must = 1,2,3, or 4") {
    forAll(larGen) { lar =>
      whenever(lar.id == 2) {
        V220(lar.loan) mustBe Success()
      }
    }
  }

  val badLoanTypeGen: Gen[Int] = intOutsideRange(1, 4)

  property("Loan Type other than 1,2,3,4 is invalid") {
    forAll(larGen, badLoanTypeGen) { (lar: LoanApplicationRegister, x: Int) =>
      val invalidLoan: Loan = lar.loan.copy(loanType = x)
      V220(invalidLoan) mustBe Failure("is not contained in valid values domain")
    }
  }

  override def check: EditCheck[LoanApplicationRegister] = V220
}
