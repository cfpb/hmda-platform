package hmda.validation.rules.lar.quality

import com.typesafe.config.ConfigFactory
import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.{ BadValueUtils, LarEditCheckSpec }
import org.scalacheck.Gen

class Q025Spec extends LarEditCheckSpec with BadValueUtils {
  val config = ConfigFactory.load()
  val loanAmount = config.getInt("hmda.validation.quality.Q025.loan.amount")

  property(s"All lars with loan amounts > $loanAmount must pass") {
    forAll(larGen, Gen.choose(loanAmount + 1, Int.MaxValue)) { (lar, x) =>
      val newLoan = lar.loan.copy(amount = x)
      val newLar = lar.copy(loan = newLoan)
      newLar.mustPass
    }
  }

  property("Whenever loan purpose is not equal to 1, lar must pass") {
    forAll(larGen, intOtherThan(1)) { (lar, x) =>
      val newLoan = lar.loan.copy(purpose = x)
      val newLar = lar.copy(loan = newLoan)
      newLar.mustPass
    }
  }

  property("Whenever property type is not equal to 1, lar must pass") {
    forAll(larGen, intOtherThan(1)) { (lar, x) =>
      val newLoan = lar.loan.copy(propertyType = x)
      val newLar = lar.copy(loan = newLoan)
      newLar.mustPass
    }
  }

  property(s"A lar with purpose and property type = 1, and loan amount <= $loanAmount must fail") {
    forAll(larGen, Gen.choose(0, loanAmount)) { (lar, x) =>
      val newLoan = lar.loan.copy(propertyType = 1, purpose = 1, amount = x)
      val newLar = lar.copy(loan = newLoan)
      newLar.mustFail
    }
  }

  override def check: EditCheck[LoanApplicationRegister] = Q025
}
