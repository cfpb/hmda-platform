package hmda.validation.rules.lar.validity

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec
import org.scalacheck.Gen

class V338Spec extends LarEditCheckSpec {
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
    forAll(larGen, actionTakenGen, incomeGen) { (lar: LoanApplicationRegister, x: Int, y: Int) =>
      val applicant = lar.applicant.copy(ethnicity = 4, race1 = 7, sex = 4, coEthnicity = 5,
        coRace1 = 8, coSex = 5, income = y.toString)
      val invalidLar = lar.copy(applicant = applicant, actionTakenType = x)
      invalidLar.mustFail
    }
  }

  val invalidEthnicityGen: Gen[Int] = Gen.choose(1, 3)

  property("Applicant not meeting the ethnicity criteria must pass") {
    forAll(larGen, actionTakenGen, invalidEthnicityGen) { (lar: LoanApplicationRegister, y: Int, x: Int) =>
      val applicant = lar.applicant.copy(ethnicity = x, race1 = 7, sex = 4, coEthnicity = 5,
        coRace1 = 8, coSex = 5, income = "whatever")
      val newLar = lar.copy(applicant = applicant, actionTakenType = y)
      newLar.mustPass
    }
  }

  val invalidRaceGen: Gen[Int] = Gen.choose(1, 6)

  property("Applicant not meeting the race criteria must pass") {
    forAll(larGen, actionTakenGen, invalidRaceGen) { (lar: LoanApplicationRegister, y: Int, x: Int) =>
      val applicant = lar.applicant.copy(ethnicity = 4, race1 = x, sex = 4, coEthnicity = 5,
        coRace1 = 8, coSex = 5, income = "whatever")
      val newLar = lar.copy(applicant = applicant, actionTakenType = y)
      newLar.mustPass
    }
  }

  val invalidSexGen: Gen[Int] = Gen.choose(1, 3)

  property("Applicant not meeting the sex criteria must pass") {
    forAll(larGen, actionTakenGen, invalidSexGen) { (lar: LoanApplicationRegister, y: Int, x: Int) =>
      val applicant = lar.applicant.copy(ethnicity = 4, race1 = 7, sex = x, coEthnicity = 5,
        coRace1 = 8, coSex = 5, income = "whatever")
      val newLar = lar.copy(applicant = applicant, actionTakenType = y)
      newLar.mustPass
    }
  }

  val invalidCoEthnicityGen: Gen[Int] = Gen.choose(1, 4)

  property("Applicant not meeting the co-ethnicity criteria must pass") {
    forAll(larGen, actionTakenGen, invalidCoEthnicityGen) { (lar: LoanApplicationRegister, y: Int, x: Int) =>
      val applicant = lar.applicant.copy(ethnicity = 4, race1 = 7, sex = 4, coEthnicity = x,
        coRace1 = 8, coSex = 5, income = "whatever")
      val newLar = lar.copy(applicant = applicant, actionTakenType = y)
      newLar.mustPass
    }
  }

  val invalidCoRaceGen: Gen[Int] = Gen.choose(1, 7)

  property("Applicant not meeting the co-race criteria must pass") {
    forAll(larGen, actionTakenGen, invalidCoRaceGen) { (lar: LoanApplicationRegister, y: Int, x: Int) =>
      val applicant = lar.applicant.copy(ethnicity = 4, race1 = 7, sex = 4, coEthnicity = 5,
        coRace1 = x, coSex = 5, income = "whatever")
      val newLar = lar.copy(applicant = applicant, actionTakenType = y)
      newLar.mustPass
    }
  }

  val invalidCoSexGen: Gen[Int] = Gen.choose(1, 4)

  property("Applicant not meeting the co-sex criteria must pass") {
    forAll(larGen, actionTakenGen, invalidCoSexGen) { (lar: LoanApplicationRegister, y: Int, x: Int) =>
      val applicant = lar.applicant.copy(ethnicity = 4, race1 = 7, sex = 4, coEthnicity = 5,
        coRace1 = 8, coSex = x, income = "whatever")
      val newLar = lar.copy(applicant = applicant, actionTakenType = y)
      newLar.mustPass
    }
  }

  property("LAR not meeting the actionTakenType criteria must pass") {
    forAll(larGen) { (lar: LoanApplicationRegister) =>
      val applicant = lar.applicant.copy(ethnicity = 4, race1 = 7, sex = 4, coEthnicity = 5,
        coRace1 = 8, coSex = 5, income = "whatever")
      val newLar = lar.copy(applicant = applicant, actionTakenType = 6)
      newLar.mustPass
    }
  }

  override def check: EditCheck[LoanApplicationRegister] = V338
}
