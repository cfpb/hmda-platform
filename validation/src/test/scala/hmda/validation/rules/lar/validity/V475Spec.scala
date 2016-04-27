package hmda.validation.rules.lar.validity

import hmda.validation.dsl.{ Failure, Success }
import hmda.validation.rules.lar.LarEditCheckSpec
import org.scalacheck.Gen

class V475Spec extends LarEditCheckSpec {
  import Gen.posNum

  property("Succeeds when applicant race1 is not 6 or 7") {
    forAll(larGen, posNum[Int]) { (lar, race1) =>
      whenever(race1 != 6 && race1 != 7) {
        val applicant = lar.applicant.copy(race1 = race1)
        val newLar = lar.copy(applicant = applicant)
        V475(newLar) mustBe a[Success]
      }
    }
  }

  val race1Gen: Gen[Int] = Gen.choose(6, 7)

  property("Succeeds when applicant race1 is 6-7 and race2-race5 are blank") {
    forAll(larGen, race1Gen) { (lar, race1) =>
      val applicant = lar.applicant.copy(race1 = race1, race2 = "", race3 = "", race4 = "", race5 = "")
      val newLar = lar.copy(applicant = applicant)
      V475(newLar) mustBe a[Success]
    }
  }

  property("Fails when applicant race1 is 6-7 and race2 is not blank (race3,4,5 blank)") {
    forAll(larGen, race1Gen, posNum[Int]) { (lar, race1, race2) =>
      val applicant = lar.applicant.copy(race1 = race1, race2 = race2.toString, race3 = "", race4 = "", race5 = "")
      val newLar = lar.copy(applicant = applicant)
      V475(newLar) mustBe a[Failure]
    }
  }

  property("Fails when applicant race1 is 6-7 and race3 is not blank (race2,4,5 blank)") {
    forAll(larGen, race1Gen, posNum[Int]) { (lar, race1, race3) =>
      val applicant = lar.applicant.copy(race1 = race1, race3 = race3.toString, race2 = "", race4 = "", race5 = "")
      val newLar = lar.copy(applicant = applicant)
      V475(newLar) mustBe a[Failure]
    }
  }

  property("Fails when applicant race1 is 6-7 and race4 is not blank (race2,3,5 blank)") {
    forAll(larGen, race1Gen, posNum[Int]) { (lar, race1, race4) =>
      val applicant = lar.applicant.copy(race1 = race1, race4 = race4.toString, race2 = "", race3 = "", race5 = "")
      val newLar = lar.copy(applicant = applicant)
      V475(newLar) mustBe a[Failure]
    }
  }

  property("Fails when applicant race1 is 6-7 and race5 is not blank (race2,3,4 blank)") {
    forAll(larGen, race1Gen, posNum[Int]) { (lar, race1, race5) =>
      val applicant = lar.applicant.copy(race1 = race1, race5 = race5.toString, race2 = "", race3 = "", race4 = "")
      val newLar = lar.copy(applicant = applicant)
      V475(newLar) mustBe a[Failure]
    }
  }

}
