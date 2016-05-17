package hmda.validation.rules.lar.validity

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.dsl.{ Failure, Success }
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec
import org.scalacheck.Gen

class V335Spec extends LarEditCheckSpec {
  property("Succeeds when property type is not 3") {
    forAll(larGen) { lar =>
      whenever(lar.loan.propertyType != 3) {
        V335(lar) mustBe Success()
      }
    }
  }

  property("Succeeds when property type is 3 and income is 'NA'") {
    forAll(larGen) { (lar: LoanApplicationRegister) =>
      val loan = lar.loan.copy(propertyType = 3)
      val applicant = lar.applicant.copy(income = "NA")
      val validLar = lar.copy(applicant = applicant, loan = loan)
      V335(validLar) mustBe Success()
    }
  }

  val incomeNumberGen: Gen[Int] = Gen.choose(Integer.MIN_VALUE, Integer.MAX_VALUE)

  property("Fails when property type is 3 and income is not 'NA'") {
    forAll(larGen, incomeNumberGen) { (lar: LoanApplicationRegister, x: Int) =>
      val loan = lar.loan.copy(propertyType = 3)
      val invalidApplicant = lar.applicant.copy(income = x.toString)
      val invalidLar = lar.copy(applicant = invalidApplicant, loan = loan)
      V335(invalidLar) mustBe a[Failure]
    }
  }
  override def check: EditCheck[LoanApplicationRegister] = V335
}
