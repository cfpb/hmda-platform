package hmda.validation.rules.lar.validity

import hmda.model.fi.lar.{ Loan, LoanApplicationRegister }
import hmda.validation.dsl.{ Failure, Success }
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec
import org.scalacheck.Gen

class V330Spec extends LarEditCheckSpec {
  property("Income must be greater than 0 or 'NA'") {
    forAll(larGen) { lar =>
      V330(lar) mustBe Success()
    }
  }

  val badIncomeNumberGen: Gen[Int] = Gen.choose(Integer.MIN_VALUE, 0)

  property("Income less than or equal to 0 is invalid") {
    forAll(larGen, badIncomeNumberGen) { (lar: LoanApplicationRegister, x: Int) =>
      val invalidApplicant = lar.applicant.copy(income = x.toString)
      val invalidLar = lar.copy(applicant = invalidApplicant)
      V330(invalidLar) mustBe a[Failure]
    }
  }

  val badIncomeStringGen: Gen[String] = Gen.alphaStr suchThat (_ != "NA")

  property("Any string aside from NA is invalid") {
    forAll(larGen, badIncomeStringGen) { (lar: LoanApplicationRegister, x: String) =>
      val invalidApplicant = lar.applicant.copy(income = x)
      val invalidLar = lar.copy(applicant = invalidApplicant)
      V330(invalidLar) mustBe a[Failure]
    }
  }

  override def check: EditCheck[LoanApplicationRegister] = V330
}
