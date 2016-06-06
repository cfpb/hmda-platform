package hmda.validation.rules.lar.quality

import com.typesafe.config.ConfigFactory
import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.{ BadValueUtils, LarEditCheckSpec }
import org.scalacheck.Gen

class Q013Spec extends LarEditCheckSpec with BadValueUtils {

  val config = ConfigFactory.load()
  val minAmount = config.getInt("hmda.validation.quality.Q013.loan.min-amount")
  val maxAmount = config.getInt("hmda.validation.quality.Q013.loan.max-amount")

  property("Valid if property type not 3") {
    forAll(larGen, intOtherThan(3)) { (lar, x) =>
      val newLoan = lar.loan.copy(propertyType = x)
      val newLar = lar.copy(loan = newLoan)
      newLar.mustPass
    }
  }

  property("Valid if loan amount between 100 ($100,000) and 10000 ($10,000,000)") {
    forAll(larGen, Gen.choose(minAmount, maxAmount)) { (lar, x) =>
      val newLoan = lar.loan.copy(amount = x)
      val newLar = lar.copy(loan = newLoan)
      newLar.mustPass
    }
  }

  property("Invalid when loan not between 100 ($100,000) and 10000 ($10,000,000) and propety type 3") {
    forAll(larGen, intOutsideRange(minAmount, maxAmount)) { (lar, x) =>
      val newLoan = lar.loan.copy(propertyType = 3, amount = x)
      val newLar = lar.copy(loan = newLoan)
      newLar.mustFail
    }
  }

  override def check: EditCheck[LoanApplicationRegister] = Q013
}
