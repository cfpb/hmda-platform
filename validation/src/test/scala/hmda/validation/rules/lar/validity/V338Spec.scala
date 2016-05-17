package hmda.validation.rules.lar.validity

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec
import org.scalacheck.Gen

class V338Spec extends LarEditCheckSpec {
  property("All applicants not meeting the criteria must pass") {
    forAll(larGen) { lar =>
      whenever(applicantNotOk(lar)) {
        lar.mustPass
      }
    }
  }

  val okAction = List(1, 2, 3, 4, 5, 7, 8)
  val actionTakenGen: Gen[Int] = Gen.oneOf(okAction)

  property("Applicant meeting the criteria with the correct income must pass") {
    forAll(larGen, actionTakenGen) { (lar: LoanApplicationRegister, x: Int) =>
      val applicant = lar.applicant.copy(ethnicity = 4, race1 = 7, sex = 4, coEthnicity = 5,
        coRace1 = 8, coSex = 5, income = "NA")
      val validLar = lar.copy(applicant = applicant, actionTakenType = x)
      validLar.mustPass
    }
  }

  val incomeGen: Gen[Int] = Gen.choose(Int.MinValue, Int.MaxValue)

  property("Applicant meeting the criteria with an incorrect income must fail") {
    forAll(larGen, actionTakenGen, incomeGen) { (lar: LoanApplicationRegister, x: Int, income: Int) =>
      val applicant = lar.applicant.copy(ethnicity = 4, race1 = 7, sex = 4, coEthnicity = 5,
        coRace1 = 8, coSex = 5, income = income.toString)
      val invalidLar = lar.copy(applicant = applicant, actionTakenType = x)
      invalidLar.mustFail
    }
  }

  override def check: EditCheck[LoanApplicationRegister] = V338

  private def applicantNotOk(lar: LoanApplicationRegister): Boolean = {
    (lar.applicant.ethnicity != 4) ||
      (lar.applicant.race1 != 7) ||
      (lar.applicant.sex != 4) ||
      (lar.applicant.coEthnicity != 5) ||
      (lar.applicant.coRace1 != 8) ||
      (lar.applicant.coSex != 5) ||
      !(okAction contains lar.actionTakenType)
  }
}
