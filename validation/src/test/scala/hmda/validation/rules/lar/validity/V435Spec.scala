package hmda.validation.rules.lar.validity

import hmda.model.fi.lar.{ Loan, LoanApplicationRegister }
import hmda.validation.dsl.{ Failure, Success }
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec
import org.scalacheck.Gen

class V435Spec extends LarEditCheckSpec with BadValueUtils {
  property("Valid if preapprovals = 1") {
    forAll(larGen) { lar =>
      val validLar = lar.copy(preapprovals = 1)
      validLar.mustPass
    }
  }

  val validActionType: Gen[Int] = intOutsideRange(7, 8)

  property("Valid if loan purpose not 1 or 2") {
    forAll(larGen, validActionType) { (lar, x) =>
      val validLar = lar.copy(actionTakenType = x)
      validLar.mustPass
    }
  }

  val invalidActionType: Gen[Int] = Gen.oneOf(7, 8)
  val invalidPreApproval: Gen[Int] = Gen.choose(Int.MinValue, Int.MaxValue).filter(_ != 1)

  property("HOEPA status other than 1, or 2 is invalid") {
    forAll(larGen, invalidActionType, invalidPreApproval) { (lar: LoanApplicationRegister, ac: Int, pr: Int) =>
      val invalidLar: LoanApplicationRegister = lar.copy(
        actionTakenType = ac,
        preapprovals = pr
      )
      invalidLar.mustFail
    }
  }

  override def check: EditCheck[LoanApplicationRegister] = V435
}
