package hmda.validation.rules.lar.quality

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.{ BadValueUtils, LarEditCheckSpec }
import org.scalacheck.Gen

class Q002Spec extends LarEditCheckSpec with BadValueUtils {

  property("Valid with non-numeric income") {
    forAll(larGen, Gen.alphaStr) { (lar, x) =>
      val newApplicant = lar.applicant.copy(income = x)
      val newLar = lar.copy(applicant = newApplicant)
      newLar.mustPass
    }
  }

  val irrelevantPropertyType: Gen[Int] = intOtherThan(1)

  property("Valid whenever property type not equal to 1") {
    forAll(larGen, irrelevantPropertyType) { (lar, x) =>
      val newLoan = lar.loan.copy(propertyType = x)
      val newLar = lar.copy(loan = newLoan)
      newLar.mustPass
    }
  }

  val irrelevantIncome: Gen[Int] = Gen.choose(200, Int.MaxValue)

  property("Valid whenever Income greater than 200 ($200,00)") {
    forAll(larGen, irrelevantIncome) { (lar, x) =>
      val newApplicant = lar.applicant.copy(income = x.toString)
      val newLar = lar.copy(applicant = newApplicant)
      newLar.mustPass
    }
  }

  val validLoan: Gen[Int] = Gen.choose(Int.MinValue, 1999)

  property("Valid when loan less than 2000 ($2 million") {
    forAll(larGen, validLoan) { (lar, x) =>
      val newLoan = lar.loan.copy(amount = x)
      val newLar = lar.copy(loan = newLoan)
      newLar.mustPass
    }
  }

  val relevantIncome: Gen[Int] = Gen.choose(0, 199)
  val invalidLoan: Gen[Int] = Gen.choose(2000, Int.MaxValue)

  property("Invalid when conditions met and loan greater than 2000") {
    forAll(larGen, relevantIncome, invalidLoan) { (lar, i, l) =>
      val newLoan = lar.loan.copy(amount = l, propertyType = 1)
      val newApplicant = lar.applicant.copy(income = i.toString)
      val newLar = lar.copy(loan = newLoan, applicant = newApplicant)
      newLar.mustFail
    }
  }

  override def check: EditCheck[LoanApplicationRegister] = Q002
}
