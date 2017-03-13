package hmda.validation.rules.lar.validity

import hmda.model.fi.lar.{ Applicant, LoanApplicationRegister }
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec
import org.scalacheck.Gen

class V485Spec extends LarEditCheckSpec {
  import Gen.posNum

  property("Succeeds when Applicant coRace1 is not in 1-5") {
    forAll(larGen, posNum[Int]) { (lar, coRace) =>
      whenever(coRace > 5) {
        val applicant = lar.applicant.copy(coRace1 = coRace)
        val newLar = lar.copy(applicant = applicant)
        newLar.mustPass
      }
    }
  }

  val coRace1Gen: Gen[Int] = Gen.choose(1, 5)

  val otherCoRaceGen: Gen[String] = Gen.oneOf("1", "2", "3", "4", "5", "")

  property("Succeeds when Applicant coRace1 is in 1-5 and other coRace fields are in 1-5 and/or blank") {
    forAll(larGen, coRace1Gen, Gen.listOfN(4, otherCoRaceGen)) { (lar, cr1, crVals) =>
      val applicant = lar.applicant.copy(coRace1 = cr1, coRace2 = crVals.head, coRace3 = crVals(1), coRace4 = crVals(2), coRace5 = crVals(3))
      val newLar = lar.copy(applicant = applicant)
      newLar.mustPass
    }
  }

  property("Fails when coRace1 is 1-5 and all coRace fields valid (1-5 or '') except coRace2") {
    forAll(larGen, coRace1Gen, posNum[Int]) {
      (lar, cr1, cr2) =>
        whenever(cr2 > 5) {
          val validApplicant = withValidCoRace2To5(lar.applicant)
          val invalidApplicant = validApplicant.copy(coRace1 = cr1, coRace2 = cr2.toString)
          val newLar = lar.copy(applicant = invalidApplicant)
          newLar.mustFail
        }
    }
  }

  property("Fails when coRace1 is 1-5 and all coRace fields valid (1-5 or '') except coRace3") {
    forAll(larGen, coRace1Gen, posNum[Int]) { (lar, cr1, cr3) =>
      whenever(cr3 > 5) {
        val validApplicant = withValidCoRace2To5(lar.applicant)
        val invalidApplicant = validApplicant.copy(coRace1 = cr1, coRace3 = cr3.toString)
        val newLar = lar.copy(applicant = invalidApplicant)
        newLar.mustFail
      }
    }
  }

  property("Fails when coRace1 is 1-5 and all coRace fields valid (1-5 or '') except coRace4") {
    forAll(larGen, coRace1Gen, posNum[Int]) { (lar, cr1, cr4) =>
      whenever(cr4 > 5) {
        val validApplicant = withValidCoRace2To5(lar.applicant)
        val invalidApplicant = validApplicant.copy(coRace1 = cr1, coRace4 = cr4.toString)
        val newLar = lar.copy(applicant = invalidApplicant)
        newLar.mustFail
      }
    }
  }

  property("Fails when coRace1 is 1-5 and all coRace fields valid (1-5 or '') except coRace5") {
    forAll(larGen, coRace1Gen, posNum[Int]) { (lar, cr1, cr5) =>
      whenever(cr5 > 5) {
        val validApplicant = withValidCoRace2To5(lar.applicant)
        val invalidApplicant = validApplicant.copy(coRace1 = cr1, coRace5 = cr5.toString)
        val newLar = lar.copy(applicant = invalidApplicant)
        newLar.mustFail
      }
    }
  }

  private def withValidCoRace2To5(applicant: Applicant): Applicant = {
    val vals: List[String] = Gen.listOfN(4, otherCoRaceGen).sample.getOrElse(List[String]())
    applicant.copy(coRace2 = vals(0), coRace3 = vals(1), coRace4 = vals(2), coRace5 = vals(3))
  }

  override def check: EditCheck[LoanApplicationRegister] = V485
}
