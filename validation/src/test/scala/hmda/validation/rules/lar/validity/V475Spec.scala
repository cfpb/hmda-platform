package hmda.validation.rules.lar.validity

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.dsl.{ Failure, Success }
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec
import org.scalacheck.Gen

class V475Spec extends RaceEditCheckSpec {
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
    succeedsWithRace(List("6", "", "", "", ""))
    succeedsWithRace(List("7", "", "", "", ""))
  }

  property("Fails when applicant race1 is 6-7 and race2 is not blank (race3,4,5 blank)") {
    failsWithRace(List("6", "1", "", "", ""))
    failsWithRace(List("7", "-4", "", "", ""))
  }

  property("Fails when applicant race1 is 6-7 and race3 is not blank (race2,4,5 blank)") {
    failsWithRace(List("6", "", "0", "", ""))
    failsWithRace(List("7", "", "5", "", ""))
  }

  property("Fails when applicant race1 is 6-7 and race4 is not blank (race2,3,5 blank)") {
    failsWithRace(List("6", "", "", "2", ""))
    failsWithRace(List("7", "", "", "11", ""))
  }

  property("Fails when applicant race1 is 6-7 and race5 is not blank (race2,3,4 blank)") {
    failsWithRace(List("6", "", "", "", "4"))
    failsWithRace(List("7", "", "", "", "9"))
  }

  override def check: EditCheck[LoanApplicationRegister] = V475
}
