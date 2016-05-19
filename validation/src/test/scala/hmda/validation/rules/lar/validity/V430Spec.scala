package hmda.validation.rules.lar.validity

import hmda.model.fi.lar.{ Loan, LoanApplicationRegister }
import hmda.validation.dsl.{ Failure, Success }
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec
import org.scalacheck.Gen

class V430Spec extends LarEditCheckSpec with BadValueUtils {
  property("Valid if preapprovals = 3") {
    forAll(larGen) { lar =>
      val validLar = lar.copy(preapprovals = 3)
      validLar.mustPass
    }
  }

  val validPurpose: Gen[Int] = intOutsideRange(1, 2)

  property("Valid if loan purpose not 1 or 2") {
    forAll(larGen, validPurpose) { (lar, x) =>
      val validLoan = lar.loan.copy(purpose = x)
      val validLar = lar.copy(loan = validLoan)
      validLar.mustPass
    }
  }

  val invalidPurpose: Gen[Int] = Gen.oneOf(1, 2)
  val invalidPreApproval: Gen[Int] = Gen.choose(Int.MinValue, Int.MaxValue).filter(_ != 3)

  property("HOEPA status other than 1, or 2 is invalid") {
    forAll(larGen, invalidPurpose, invalidPreApproval) { (lar: LoanApplicationRegister, pu: Int, pr: Int) =>
      val invalidLoan = lar.loan.copy(purpose = pu)
      val invalidLar: LoanApplicationRegister = lar.copy(
        loan = invalidLoan,
        preapprovals = pr
      )
      invalidLar.mustFail
    }
  }

  override def check: EditCheck[LoanApplicationRegister] = V430
}
