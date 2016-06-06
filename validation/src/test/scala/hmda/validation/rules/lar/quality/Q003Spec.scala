package hmda.validation.rules.lar.quality

import com.typesafe.config.ConfigFactory
import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.{ BadValueUtils, LarEditCheckSpec }
import org.scalacheck.Gen

class Q003Spec extends LarEditCheckSpec with BadValueUtils {

  val config = ConfigFactory.load()
  val loanAmount = config.getInt("hmda.validation.quality.Q003.loan.amount")

  val irrelevantLoanType = intOtherThan(2)

  property("Valid with loan type not 2") {
    forAll(larGen, irrelevantLoanType) { (lar, x) =>
      val newLoan = lar.loan.copy(loanType = x)
      val newLar = lar.copy(loan = newLoan)
      newLar.mustPass
    }
  }

  val irrelevantPropertyType: Gen[Int] = intOtherThan(List(1, 2))

  property("Valid whenever property type not equal to 1 or 2") {
    forAll(larGen, irrelevantPropertyType) { (lar, x) =>
      val newLoan = lar.loan.copy(propertyType = x)
      val newLar = lar.copy(loan = newLoan)
      newLar.mustPass
    }
  }

  val validLoan: Gen[Int] = Gen.choose(Int.MinValue, loanAmount)

  property("Valid when loan less than 626 ($626,000") {
    forAll(larGen, validLoan) { (lar, x) =>
      val newLoan = lar.loan.copy(amount = x)
      val newLar = lar.copy(loan = newLoan)
      newLar.mustPass
    }
  }

  val relevantPropertyType: Gen[Int] = Gen.oneOf(1, 2)
  val invalidLoan: Gen[Int] = Gen.choose(loanAmount + 1, Int.MaxValue)

  property("Invalid when conditions met and loan greater than 626") {
    forAll(larGen, relevantPropertyType, invalidLoan) { (lar, p, l) =>
      val newLoan = lar.loan.copy(loanType = 2, propertyType = p, amount = l)
      val newLar = lar.copy(loan = newLoan)
      newLar.mustFail
    }
  }

  override def check: EditCheck[LoanApplicationRegister] = Q003
}
