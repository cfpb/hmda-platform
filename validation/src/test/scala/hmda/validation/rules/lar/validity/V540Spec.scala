package hmda.validation.rules.lar.validity

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec
import org.scalacheck.Gen

class V540Spec extends LarEditCheckSpec with BadValueUtils {

  val relevantActions = List(2, 3, 4, 5, 7, 8)

  val irrelevantActionGen: Gen[Int] =
    Gen.choose(Int.MinValue, Int.MaxValue)
      .filter(!relevantActions.contains(_))

  property("Valid when Action Taken not 2,3,4,5,7 or 8") {
    forAll(larGen, irrelevantActionGen) { (lar, x) =>
      val validLar = lar.copy(actionTakenType = x)
      validLar.mustPass
    }
  }

  property("Valid when HOEPA status is 2") {
    forAll(larGen) { lar =>
      val validLar = lar.copy(hoepaStatus = 2)
      validLar.mustPass
    }
  }

  val relevantActionGen: Gen[Int] = Gen.oneOf(relevantActions)
  val badHoepaStatusGen: Gen[Int] = Gen.choose(Int.MinValue, Int.MaxValue).filter(_ != 2)

  property("HOEPA status other than 2 is invalid when action taken = 2,3,4,5,7,8") {
    forAll(larGen, badHoepaStatusGen, relevantActionGen) { (lar: LoanApplicationRegister, hs: Int, at: Int) =>
      val invalidLar: LoanApplicationRegister = lar.copy(
        actionTakenType = at,
        hoepaStatus = hs
      )
      invalidLar.mustFail
    }
  }

  override def check: EditCheck[LoanApplicationRegister] = V540
}
