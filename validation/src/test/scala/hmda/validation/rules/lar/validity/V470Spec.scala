package hmda.validation.rules.lar.validity

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.rules.lar.LarEditCheckSpec
import org.scalacheck.Gen

class V470Spec extends LarEditCheckSpec {
  import Gen.posNum

  property("Succeeds when Applicant Race1 is not in 1-5") {
    forAll(larGen, posNum[Int]) { (lar, race) =>
      whenever(race > 5) {
        val applicant = lar.applicant.copy(race1 = race)
        val newLar = lar.copy(applicant = applicant)
        V470(newLar) mustBe a[Success]
      }
    }
  }

  val raceFieldsGen: Gen[String] = {
    Gen.listOfN(4, optional(Gen.choose(1, 5)))
  }

  property("Succeeds when Applicant Race1 is in 1-5 and other race fields are in 1-5 and/or blank") {
    forAll(larGen, raceFieldsGen) { (lar: LoanApplicationRegister, fields: List[String]) =>
      val applicant = lar.applicant.copy()
      val newLar = lar.copy(applicant = applicant)
      V470(newLar) mustBe a[Success]
    }
  }

  property("Fails when Applicant Race1 is in 1-5 and other race fields have value not in 1-5")

}
