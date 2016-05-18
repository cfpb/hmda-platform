package hmda.validation.rules.lar.validity

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.dsl.{ Failure, Success }
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec
import org.scalacheck.Gen

class V315Spec extends LarEditCheckSpec with BadValueUtils {

  property("Succeeds when Applicant CoRace1 is 1,2,3,4,5,6,7,8") {
    forAll(larGen) { lar =>
      V315(lar) mustBe Success()
    }
  }

  val badApplicantRaceGen: Gen[Int] = intOutsideRange(1, 8)

  property("Fails when Co-Applicant Race1 (applicant coRace1) is < 1 or > 8") {
    forAll(larGen, badApplicantRaceGen) { (lar, cr1) =>
      val invalidApplicant = lar.applicant.copy(coRace1 = cr1)
      val invalidLar = lar.copy(applicant = invalidApplicant)
      V315(invalidLar) mustBe a[Failure]
    }
  }

  override def check: EditCheck[LoanApplicationRegister] = V315
}
