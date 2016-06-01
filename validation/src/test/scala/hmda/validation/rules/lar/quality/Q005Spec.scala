package hmda.validation.rules.lar.quality

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.{BadValueUtils, LarEditCheckSpec}
import org.scalacheck.Gen

class Q005Spec extends LarEditCheckSpec with BadValueUtils {
  property("Whenever purchaser type and property type are relevant, loan amount must be <= 1203") {
    forAll(larGen) { lar =>
      lar.mustPass
    }
  }

  val irrelevantPurchaser: Gen[Int] = intOutsideRange(1, 4)

  property("Whenever purchaser type is irrelevant, lar must pass") {
    forAll(larGen, irrelevantPurchaser) { (lar, x) =>
      val newLar = lar.copy(purchaserType = x)
      newLar.mustPass
    }
  }

  val irrelevantProperty: Gen[Int] = intOutsideRange(1, 2)

  property("Whenever property type is irrelevant, lar must pass") {
    forAll(larGen, irrelevantProperty) { (lar, x) =>
      val newLoan = lar.loan.copy(propertyType = x)
      val newLar = lar.copy(loan = newLoan)
      newLar.mustPass
    }
  }

  property("A lar with ")

  override def check: EditCheck[LoanApplicationRegister] = Q005
}
