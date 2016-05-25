package hmda.validation.rules.lar.validity

import hmda.model.fi.lar.{ Applicant, LoanApplicationRegister }
import hmda.validation.rules.lar.LarEditCheckSpec

abstract class RaceEditCheckSpec extends LarEditCheckSpec {

  def succeedsWithRace(vals: List[String]) = {
    forAll(larGen) { lar: LoanApplicationRegister =>
      val testApplicant = applicantWithRaceVals(lar.applicant, vals)
      val testLar = lar.copy(applicant = testApplicant)
      testLar.mustPass
    }
  }
  def succeedsWithCoRace(vals: List[String]) = {
    forAll(larGen) { lar: LoanApplicationRegister =>
      val testApplicant = applicantWithCoRaceVals(lar.applicant, vals)
      val testLar = lar.copy(applicant = testApplicant)
      testLar.mustPass
    }
  }

  def failsWithRace(vals: List[String]) = {
    forAll(larGen) { lar: LoanApplicationRegister =>
      val testApplicant = applicantWithRaceVals(lar.applicant, vals)
      val testLar = lar.copy(applicant = testApplicant)
      testLar.mustFail
    }
  }
  def failsWithCoRace(vals: List[String]) = {
    forAll(larGen) { lar: LoanApplicationRegister =>
      val testApplicant = applicantWithCoRaceVals(lar.applicant, vals)
      val testLar = lar.copy(applicant = testApplicant)
      testLar.mustFail
    }
  }

  private def applicantWithRaceVals(applicant: Applicant, vals: List[String]): Applicant = {
    applicant.copy(
      race1 = vals(0).toInt,
      race2 = vals(1),
      race3 = vals(2),
      race4 = vals(3),
      race5 = vals(4)
    )
  }
  private def applicantWithCoRaceVals(applicant: Applicant, vals: List[String]): Applicant = {
    applicant.copy(
      coRace1 = vals(0).toInt,
      coRace2 = vals(1),
      coRace3 = vals(2),
      coRace4 = vals(3),
      coRace5 = vals(4)
    )
  }

}
