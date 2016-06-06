package hmda.validation.rules.lar.quality

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.{ BadValueUtils, LarEditCheckSpec }
import org.scalacheck.Gen

class Q001Spec extends LarEditCheckSpec with BadValueUtils {

  property("Valid with non-numeric income") {
    forAll(larGen, Gen.alphaStr) { (lar, x) =>
      val newApplicant = lar.applicant.copy(income = x)
      val newLar = lar.copy(applicant = newApplicant)
      newLar.mustPass
    }
  }

  val irrelevantLoanAmount: Gen[Int] = Gen.choose(Int.MinValue, 999)

  property("Valid whenever loan less than 1000 ($1 million)") {
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
  val validMultiplier: Gen[Int] = Gen.choose(1, 4)

  property("Valid when loan less than five times income") {
    forAll(larGen, relevantIncome, validMultiplier) { (lar, x, m) =>
      val newLoan = lar.loan.copy(amount = x * m)
      val newApplicant = lar.applicant.copy(income = x.toString)
      val newLar = lar.copy(loan = newLoan, applicant = newApplicant)
      newLar.mustPass
    }
  }

  val invalidMultiplier: Gen[Int] = Gen.choose(5, 100)

  property("Invalid when loan greater than five times income") {
    forAll(larGen, relevantIncome, invalidMultiplier) { (lar, x, m) =>
      val newLoan = lar.loan.copy(amount = x * m)
      val newApplicant = lar.applicant.copy(income = x.toString)
      val newLar = lar.copy(loan = newLoan, applicant = newApplicant)
      newLar.mustFail
    }
  }

  override def check: EditCheck[LoanApplicationRegister] = Q001
}
