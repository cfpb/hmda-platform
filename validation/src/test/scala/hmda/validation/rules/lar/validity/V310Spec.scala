package hmda.validation.rules.lar.validity

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.{ BadValueUtils, LarEditCheckSpec }
import org.scalacheck.Gen

class V310Spec extends LarEditCheckSpec with BadValueUtils {

  property("Succeeds when Applicant Race1 is 1,2,3,4,5,6,7") {
    forAll(larGen) { lar =>
      lar.mustPass
    }
  }

  val badApplicantRaceGen: Gen[Int] = {
    intOutsideRange(1, 7)
  }

  property("Fails when Applicant Race1 is < 1 or > 7") {
    forAll(larGen, badApplicantRaceGen) { (lar, r1) =>
      val invalidApplicant = lar.applicant.copy(race1 = r1)
      val invalidLar = lar.copy(applicant = invalidApplicant)
      invalidLar.mustFail
    }
  }

  override def check: EditCheck[LoanApplicationRegister] = V310
}
