package hmda.validation.rules.lar.validity

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.dsl.{ Failure, Success }
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec
import org.scalacheck.Gen

class V490Spec extends LarEditCheckSpec {
  import Gen.posNum

  property("Succeeds when applicant coRace1 is not 6,7, or 8") {
    forAll(larGen, posNum[Int]) { (lar, cr1) =>
      whenever(!(6 to 8).contains(cr1)) {
        val applicant = lar.applicant.copy(coRace1 = cr1)
        val newLar = lar.copy(applicant = applicant)
        V490(newLar) mustBe a[Success]
      }
    }
  }

  val coRace1Gen: Gen[Int] = Gen.choose(6, 8)

  property("Succeeds when applicant coRace1 is 6-8 and coRace2-5 are blank") {
    forAll(larGen, coRace1Gen) { (lar, coRace1) =>
      val applicant = lar.applicant.copy(coRace1 = coRace1, coRace2 = "", coRace3 = "", coRace4 = "", coRace5 = "")
      val newLar = lar.copy(applicant = applicant)
      V490(newLar) mustBe a[Success]
    }
  }

  property("Fails when applicant coRace1 is 6-8 and coRace2 is not blank (coRace3,4,5 blank)") {
    forAll(larGen, coRace1Gen, posNum[Int]) { (lar, cr1, cr2) =>
      val applicant = lar.applicant.copy(coRace1 = cr1, coRace2 = cr2.toString, coRace3 = "", coRace4 = "", coRace5 = "")
      val newLar = lar.copy(applicant = applicant)
      V490(newLar) mustBe a[Failure]
    }
  }

  property("Fails when applicant coRace1 is 6-8 and coRace3 is not blank (coRace2,4,5 blank)") {
    forAll(larGen, coRace1Gen, posNum[Int]) { (lar, cr1, cr3) =>
      val applicant = lar.applicant.copy(coRace1 = cr1, coRace3 = cr3.toString, coRace2 = "", coRace4 = "", coRace5 = "")
      val newLar = lar.copy(applicant = applicant)
      V490(newLar) mustBe a[Failure]
    }
  }

  property("Fails when applicant coRace1 is 6-8 and coRace4 is not blank (coRace2,3,5 blank)") {
    forAll(larGen, coRace1Gen, posNum[Int]) { (lar, cr1, cr4) =>
      val applicant = lar.applicant.copy(coRace1 = cr1, coRace4 = cr4.toString, coRace2 = "", coRace3 = "", coRace5 = "")
      val newLar = lar.copy(applicant = applicant)
      V490(newLar) mustBe a[Failure]
    }
  }

  property("Fails when applicant coRace1 is 6-8 and coRace5 is not blank (coRace2,3,4 blank)") {
    forAll(larGen, coRace1Gen, posNum[Int]) { (lar, cr1, cr5) =>
      val applicant = lar.applicant.copy(coRace1 = cr1, coRace5 = cr5.toString, coRace2 = "", coRace3 = "", coRace4 = "")
      val newLar = lar.copy(applicant = applicant)
      V490(newLar) mustBe a[Failure]
    }
  }

  override def check: EditCheck[LoanApplicationRegister] = V490
}
