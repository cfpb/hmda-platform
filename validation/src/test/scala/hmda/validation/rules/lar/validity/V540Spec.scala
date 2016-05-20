package hmda.validation.rules.lar.validity

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec
import org.scalacheck.Gen

class V540Spec extends LarEditCheckSpec with BadValueUtils {

  val badActionTakenList = List(2, 3, 4, 5, 7, 8)
  val validActionTakenGen: Gen[Int] =
    Gen.choose(Int.MinValue, Int.MaxValue)
      .filter(!badActionTakenList.contains(_))

  property("Valid Action taken not 2,3,4,5,7 or 8") {
    forAll(larGen, validActionTakenGen) { (lar, x) =>
      val validLar = lar.copy(actionTakenType = x)
      validLar.mustPass
    }
  }

  property("Valid if HOEPA status is 2") {
    forAll(larGen) { lar =>
      val validLar = lar.copy(hoepaStatus = 2)
      validLar.mustPass
    }
  }

  val badActionTakenGen: Gen[Int] = Gen.oneOf(badActionTakenList)
  val badHoepaStatusGen: Gen[Int] = Gen.choose(Int.MinValue, Int.MaxValue).filter(_ != 2)

  property("HOEPA status other than 2 is invalid with certain action types") {
    forAll(larGen, badHoepaStatusGen, badActionTakenGen) { (lar: LoanApplicationRegister, hs: Int, at: Int) =>
      val invalidLar: LoanApplicationRegister = lar.copy(
        actionTakenType = at,
        hoepaStatus = hs
      )
      invalidLar.mustFail
    }
  }

  override def check: EditCheck[LoanApplicationRegister] = V540
}
