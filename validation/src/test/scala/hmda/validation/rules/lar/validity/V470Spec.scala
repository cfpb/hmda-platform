package hmda.validation.rules.lar.validity

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.dsl.{ Failure, Success }
import hmda.validation.rules.EditCheck
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

  val race1Gen: Gen[Int] = Gen.choose(1, 5)

  val otherRaceGen: Gen[String] = Gen.oneOf("1", "2", "3", "4", "5", "")

  property("Succeeds when Applicant Race1 is in 1-5 and other race fields are in 1-5 and/or blank") {
    forAll(larGen, Gen.listOfN(4, otherRaceGen)) {
      (lar: LoanApplicationRegister, raceVals) =>
        val applicant = lar.applicant.copy(race2 = raceVals(0), race3 = raceVals(1), race4 = raceVals(2), race5 = raceVals(3))
        val newLar = lar.copy(applicant = applicant)
        V470(newLar) mustBe a[Success]
    }
  }

  property("Fails when Applicant Race1 is in 1-5 and race2 not in 1-5 or blank") {
    forAll(larGen, race1Gen, posNum[Int]) { (lar, race1, race2) =>
      whenever(race2 > 5) {
        val applicant = lar.applicant.copy(race1 = race1, race2 = race2.toString)
        val newLar = lar.copy(applicant = applicant)
        V470(newLar) mustBe a[Failure]
      }
    }
  }

  property("Fails when Applicant Race1 is in 1-5 and race3 not in 1-5 or blank") {
    forAll(larGen, race1Gen, posNum[Int]) { (lar, race1, race3) =>
      whenever(race3 > 5) {
        val applicant = lar.applicant.copy(race1 = race1, race3 = race3.toString)
        val newLar = lar.copy(applicant = applicant)
        V470(newLar) mustBe a[Failure]
      }
    }
  }

  property("Fails when Applicant Race1 is in 1-5 and race4 not in 1-5 or blank") {
    forAll(larGen, race1Gen, posNum[Int]) { (lar, race1, race4) =>
      whenever(race4 > 5) {
        val applicant = lar.applicant.copy(race1 = race1, race4 = race4.toString)
        val newLar = lar.copy(applicant = applicant)
        V470(newLar) mustBe a[Failure]
      }
    }
  }

  property("Fails when Applicant Race1 is in 1-5 and race5 not in 1-5 or blank") {
    forAll(larGen, race1Gen, posNum[Int]) { (lar, race1, race5) =>
      whenever(race5 > 5) {
        val applicant = lar.applicant.copy(race1 = race1, race5 = race5.toString)
        val newLar = lar.copy(applicant = applicant)
        V470(newLar) mustBe a[Failure]
      }
    }
  }

  override def check: EditCheck[LoanApplicationRegister] = V470
}
