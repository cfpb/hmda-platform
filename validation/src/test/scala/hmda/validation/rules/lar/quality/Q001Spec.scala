package hmda.validation.rules.lar.quality

import com.typesafe.config.ConfigFactory
import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.{ BadValueUtils, LarEditCheckSpec }
import org.scalacheck.Gen

class Q001Spec extends LarEditCheckSpec with BadValueUtils {

  val config = ConfigFactory.load()
  val loanAmount = config.getInt("hmda.validation.quality.Q001.loan.amount")
  val multiplier = config.getInt("hmda.validation.quality.Q001.incomeMultiplier")

  property("Valid with non-numeric income") {
    forAll(larGen, Gen.alphaStr) { (lar, x) =>
      val newApplicant = lar.applicant.copy(income = x)
      val newLar = lar.copy(applicant = newApplicant)
      newLar.mustPass
    }
  }

  val irrelevantLoanAmount: Gen[Int] = Gen.choose(Int.MinValue, loanAmount - 1)

  property(s"Valid whenever loan less than $loanAmount") {
    forAll(larGen, irrelevantLoanAmount) { (lar, x) =>
      val newLoan = lar.loan.copy(amount = x)
      val newLar = lar.copy(loan = newLoan)
      newLar.mustPass
    }
  }

  val irrelevantIncome: Gen[Int] = Gen.choose(Int.MinValue, 0)

  property("Valid whenever Income equal or less than 0") {
    forAll(larGen, irrelevantIncome) { (lar, x) =>
      val newApplicant = lar.applicant.copy(income = x.toString)
      val newLar = lar.copy(applicant = newApplicant)
      newLar.mustPass
    }
  }

  val relevantIncome: Gen[Int] = Gen.choose(1000, 100000)
  val validMultiplier: Gen[Int] = Gen.choose(1, multiplier - 1)

  property(s"Valid when loan less than $multiplier times income") {
    forAll(larGen, relevantIncome, validMultiplier) { (lar, x, m) =>
      val newLoan = lar.loan.copy(amount = x * m)
      val newApplicant = lar.applicant.copy(income = x.toString)
      val newLar = lar.copy(loan = newLoan, applicant = newApplicant)
      newLar.mustPass
    }
  }

  val invalidMultiplier: Gen[Int] = Gen.choose(multiplier, 100)

  property(s"Invalid when loan greater than $multiplier times income") {
    forAll(larGen, relevantIncome, invalidMultiplier) { (lar, x, m) =>
      val newLoan = lar.loan.copy(amount = x * m)
      val newApplicant = lar.applicant.copy(income = x.toString)
      val newLar = lar.copy(loan = newLoan, applicant = newApplicant)
      newLar.mustFail
    }
  }

  override def check: EditCheck[LoanApplicationRegister] = Q001
}
