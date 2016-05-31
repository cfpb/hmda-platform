package hmda.validation.rules.lar.validity

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec
import org.scalacheck.Gen

class V315Spec extends LarEditCheckSpec with BadValueUtils {

  property("Succeeds when Applicant CoRace1 is 1,2,3,4,5,6,7,8") {
    forAll(larGen) { lar =>
      lar.mustPass
    }
  }

  val badApplicantRaceGen: Gen[Int] = intOutsideRange(1, 8)

  property("Fails when Co-Applicant Race1 (applicant coRace1) is < 1 or > 8") {
    forAll(larGen, badApplicantRaceGen) { (lar, cr1) =>
      val invalidApplicant = lar.applicant.copy(coRace1 = cr1)
      val invalidLar = lar.copy(applicant = invalidApplicant)
      invalidLar.mustFail
    }
  }

  override def check: EditCheck[LoanApplicationRegister] = V315
}
