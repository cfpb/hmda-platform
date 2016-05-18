package hmda.validation.rules.lar.validity

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.dsl.{ Failure, Success }
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec
import org.scalacheck.Gen

class V455Spec extends LarEditCheckSpec {
  import Gen.posNum

  property("Succeeds when Applicant Ethnicity is not 1, 2, or 3") {
    forAll(larGen, posNum[Int]) { (lar, ethn) =>
      whenever(ethn > 3) {
        val applicant = lar.applicant.copy(ethnicity = ethn)
        val newLar = lar.copy(applicant = applicant)
        V455(newLar) mustBe a[Success]
      }
    }
  }
  property("Succeeds when Applicant Race1 is not 7") {
    forAll(larGen, posNum[Int]) { (lar, race) =>
      whenever(race != 7) {
        val applicant = lar.applicant.copy(race1 = race)
        val newLar = lar.copy(applicant = applicant)
        V455(newLar) mustBe a[Success]
      }
    }
  }

  property("Fails when Applicant Ethnicity is 1,2, or 3, and Race1 is 7") {
    forAll(larGen) { lar =>
      whenever(List(1, 2, 3).contains(lar.applicant.ethnicity)) {
        val applicant = lar.applicant.copy(race1 = 7)
        val newLar = lar.copy(applicant = applicant)
        V455(newLar) mustBe a[Failure]
      }
    }
  }

  override def check: EditCheck[LoanApplicationRegister] = V455
}
