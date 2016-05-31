package hmda.validation.rules.lar.validity

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec
import org.scalacheck.Gen

class V460Spec extends LarEditCheckSpec with BadValueUtils {
  property("CoApplicant ethnicity must = 1,2,3,4 or 5") {
    forAll(larGen) { lar =>
      lar.mustPass
    }
  }

  val badCoEthnicityGen: Gen[Int] = intOutsideRange(1, 5)

  property("Applicant ethnicity other than 1,2,3,4,5 is invalid") {
    forAll(larGen, badCoEthnicityGen) { (lar, x) =>
      val invalidApplicant = lar.applicant.copy(coEthnicity = x)
      val invalidLar = lar.copy(applicant = invalidApplicant)
      invalidLar.mustFail
    }
  }

  override def check: EditCheck[LoanApplicationRegister] = V460
}
