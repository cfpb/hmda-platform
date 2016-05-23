package hmda.validation.rules.lar.validity

import hmda.model.fi.lar.{ Loan, LoanApplicationRegister }
import hmda.validation.dsl.{ Failure, Success }
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec
import org.scalacheck.Gen

class V260Spec extends LarEditCheckSpec with BadValueUtils {
  property("Valid when action taken 3 or 7") {
    forAll(larGen, Gen.oneOf(3, 7)) { (lar, action) =>
      val validLar = lar.copy(actionTakenType = action)
      validLar.mustPass
    }
  }

  property("Valid when no denial not in range 1-9") {
    forAll(
      larGen,
      intOutsideRange(1, 9),
      intOutsideRange(1, 9),
      intOutsideRange(1, 9)
    ) { (lar, denial1, denial2, denial3) =>
        val validDenial = lar.denial.copy(
          reason1 = denial1.toString,
          reason2 = denial2.toString,
          reason3 = denial3.toString
        )
        val validLar = lar.copy(denial = validDenial)
        validLar.mustPass
      }
  }

  val actionGen = intOtherThan(List(3, 7))
  val deniedGen = Gen.choose(1, 9)
  val notDeniedGen = intOtherThan(1 to 9)
  val intGen = Gen.oneOf(deniedGen, notDeniedGen)

  property("Invalid when action taken not 3 or 7 when any denial in 1 to 9") {
    forAll(larGen, actionGen, intGen, intGen, intGen) { (lar, action, D1, D2, D3) =>
      whenever(
        D1 <= 9 && D1 >= 1 ||
          D2 <= 9 && D2 >= 1 ||
          D3 <= 9 && D3 >= 1
      ) {
          val invalidDenial = lar.denial.copy(reason1 = D1.toString, reason2 = D2.toString, reason3 = D3.toString)
          val invalidLar: LoanApplicationRegister = lar.copy(actionTakenType = action, denial = invalidDenial)
          invalidLar.mustFail
        }
    }
  }

  override def check: EditCheck[LoanApplicationRegister] = V260
}
