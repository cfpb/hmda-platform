package hmda.validation.rules.lar.validity

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.rules.EditCheck
import org.scalacheck.Gen

class V490Spec extends RaceEditCheckSpec {
  import Gen.posNum

  property("Succeeds when applicant coRace1 is not 6,7, or 8") {
    forAll(larGen, posNum[Int]) { (lar, cr1) =>
      whenever(!(6 to 8).contains(cr1)) {
        val applicant = lar.applicant.copy(coRace1 = cr1)
        val newLar = lar.copy(applicant = applicant)
        newLar.mustPass
      }
    }
  }

  val coRace1Gen: Gen[Int] = Gen.choose(6, 8)

  property("Succeeds when applicant coRace1 is 6-8 and coRace2-5 are blank") {
    succeedsWithCoRace(List("6", "", "", "", ""))
    succeedsWithCoRace(List("7", "", "", "", ""))
    succeedsWithCoRace(List("8", "", "", "", ""))
  }

  property("Fails when applicant coRace1 is 6-8 and coRace2 is not blank (coRace3,4,5 blank)") {
    failsWithCoRace(List("6", "1", "", "", ""))
    failsWithCoRace(List("7", "2", "", "", ""))
    failsWithCoRace(List("8", "3", "", "", ""))
  }

  property("Fails when applicant coRace1 is 6-8 and coRace3 is not blank (coRace2,4,5 blank)") {
    failsWithCoRace(List("6", "", "4", "", ""))
    failsWithCoRace(List("7", "", "5", "", ""))
    failsWithCoRace(List("8", "", "6", "", ""))
  }

  property("Fails when applicant coRace1 is 6-8 and coRace4 is not blank (coRace2,3,5 blank)") {
    failsWithCoRace(List("6", "", "", "7", ""))
    failsWithCoRace(List("7", "", "", "8", ""))
    failsWithCoRace(List("8", "", "", "9", ""))
  }

  property("Fails when applicant coRace1 is 6-8 and coRace5 is not blank (coRace2,3,4 blank)") {
    failsWithCoRace(List("6", "", "", "", "1"))
    failsWithCoRace(List("7", "", "", "", "2"))
    failsWithCoRace(List("8", "", "", "", "3"))
  }

  override def check: EditCheck[LoanApplicationRegister] = V490
}
