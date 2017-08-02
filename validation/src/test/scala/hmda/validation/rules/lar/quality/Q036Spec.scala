package hmda.validation.rules.lar.quality

import com.typesafe.config.ConfigFactory
import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.{ BadValueUtils, LarEditCheckSpec }
import org.scalacheck.Gen

class Q036Spec extends LarEditCheckSpec with BadValueUtils {

  val config = ConfigFactory.load()
  val loanAmount = config.getInt("hmda.validation.quality.Q036.loan.amount")

  property("Valid if property type not 2") {
    forAll(larGen, intOtherThan(2)) { (lar, x) =>
      val newLoan = lar.loan.copy(propertyType = x)
      val newLar = lar.copy(loan = newLoan)
      newLar.mustPass
    }
  }

  property(s"Valid if loan amount less than or equal to $loanAmount") {
    forAll(larGen, Gen.choose(Int.MinValue, loanAmount)) { (lar, x) =>
      val newLoan = lar.loan.copy(amount = x)
      val newLar = lar.copy(loan = newLoan)
      newLar.mustPass
    }
  }

  property(s"Invalid when loan greater than $loanAmount and propety type 2") {
    forAll(larGen, Gen.choose(loanAmount + 1, Int.MaxValue)) { (lar, x) =>
      val newLoan = lar.loan.copy(propertyType = 2, amount = x)
      val newLar = lar.copy(loan = newLoan)
      newLar.mustFail
    }
  }

  override def check: EditCheck[LoanApplicationRegister] = Q036
}
