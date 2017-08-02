package hmda.validation.rules.lar.validity

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.{ BadValueUtils, LarEditCheckSpec }
import org.scalacheck.Gen

class V425Spec extends LarEditCheckSpec with BadValueUtils {
  property("Valid if preapprovals = 3") {
    forAll(larGen) { lar =>
      val validLar = lar.copy(preapprovals = 3)
      validLar.mustPass
    }
  }

  val validActionTypeGen: Gen[Int] = Gen.choose(Int.MinValue, Int.MaxValue).filter(_ != 6)

  property("Valid if action not = 6") {
    forAll(larGen, validActionTypeGen) { (lar, x) =>
      val validLar = lar.copy(actionTakenType = x)
      validLar.mustPass
    }
  }

  val validLoanPurposeGen: Gen[Int] = Gen.choose(Int.MinValue, Int.MaxValue).filter(_ != 1)

  property("Valid if loan purpose not = 1") {
    forAll(larGen, validLoanPurposeGen) { (lar, x) =>
      val validLoan = lar.loan.copy(purpose = x)
      val validLar = lar.copy(loan = validLoan)
      validLar.mustPass
    }
  }

  val invalidPreapprovals: Gen[Int] = Gen.choose(Int.MinValue, Int.MaxValue).filter(_ != 3)

  property("Preparovals other than 3 when conditions met is invalid") {
    forAll(larGen, invalidPreapprovals) { (lar: LoanApplicationRegister, x: Int) =>
      val invalidLoan = lar.loan.copy(purpose = 1)
      val invalidLar: LoanApplicationRegister = lar.copy(
        loan = invalidLoan,
        actionTakenType = 6,
        preapprovals = x
      )
      invalidLar.mustFail
    }
  }

  override def check: EditCheck[LoanApplicationRegister] = V425
}
