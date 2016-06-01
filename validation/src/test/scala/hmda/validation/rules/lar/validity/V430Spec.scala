package hmda.validation.rules.lar.validity

import hmda.model.fi.lar.{Loan, LoanApplicationRegister}
import hmda.validation.dsl.{Failure, Success}
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.{BadValueUtils, LarEditCheckSpec}
import org.scalacheck.Gen

class V430Spec extends LarEditCheckSpec with BadValueUtils {
  property("Valid if preapprovals = 3") {
    forAll(larGen) { lar =>
      val validLar = lar.copy(preapprovals = 3)
      validLar.mustPass
    }
  }

  val validPurpose: Gen[Int] = intOutsideRange(2, 3)

  property("Valid if loan purpose not 2 or 3") {
    forAll(larGen, validPurpose) { (lar, x) =>
      val validLoan = lar.loan.copy(purpose = x)
      val validLar = lar.copy(loan = validLoan)
      validLar.mustPass
    }
  }

  val invalidPurpose: Gen[Int] = Gen.oneOf(2, 3)
  val invalidPreApproval: Gen[Int] = intOtherThan(3)

  property("Preapproval other than 3 is invalid") {
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
