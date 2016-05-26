package hmda.validation.rules.lar.validity

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.dsl.{ Failure, Success }
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec
import org.scalacheck.Gen

class V465Spec extends LarEditCheckSpec {
  import Gen.posNum

  property("Succeeds when Co-Applicant Ethnicity is not 1, 2, or 3") {
    forAll(larGen, posNum[Int]) { (lar, coEthn) =>
      whenever(coEthn > 3) {
        val applicant = lar.applicant.copy(coEthnicity = coEthn)
        val newLar = lar.copy(applicant = applicant)
        V465(newLar) mustBe a[Success]
      }
    }
  }
  property("Succeeds when Applicant Co-Race1 is not 7 or 8") {
    forAll(larGen, posNum[Int]) { (lar, cr1) =>
      whenever(cr1 != 7 && cr1 != 8) {
        val applicant = lar.applicant.copy(coRace1 = cr1)
        val newLar = lar.copy(applicant = applicant)
        V465(newLar) mustBe a[Success]
      }
    }
  }

  property("Fails when Applicant CoEthnicity is 1,2, or 3, and CoRace1 is 7 or 8") {
    forAll(larGen, Gen.choose(7, 8)) { (lar, cr1) =>
      whenever(List(1, 2, 3).contains(lar.applicant.coEthnicity)) {
        val applicant = lar.applicant.copy(coRace1 = cr1)
        val newLar = lar.copy(applicant = applicant)
        V465(newLar) mustBe a[Failure]
      }
    }
  }

  override def check: EditCheck[LoanApplicationRegister] = V465
}
