package hmda.validation.rules.lar.validity

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec
import org.scalacheck.Gen

class V330Spec extends LarEditCheckSpec {
  property("Income must be greater than 0 or 'NA'") {
    forAll(larGen) { lar =>
      lar.mustPass
    }
  }

  val badIncomeNumberGen: Gen[Int] = Gen.choose(Integer.MIN_VALUE, 0)

  property("Income less than or equal to 0 is invalid") {
    forAll(larGen, badIncomeNumberGen) { (lar: LoanApplicationRegister, x: Int) =>
      val invalidApplicant = lar.applicant.copy(income = x.toString)
      val invalidLar = lar.copy(applicant = invalidApplicant)
      invalidLar.mustFail
    }
  }

  val badIncomeStringGen: Gen[String] = Gen.alphaStr suchThat (_ != "NA")

  property("Any string aside from NA is invalid") {
    forAll(larGen, badIncomeStringGen) { (lar: LoanApplicationRegister, x: String) =>
      val invalidApplicant = lar.applicant.copy(income = x)
      val invalidLar = lar.copy(applicant = invalidApplicant)
      invalidLar.mustFail
    }
  }

  override def check: EditCheck[LoanApplicationRegister] = V330
}
