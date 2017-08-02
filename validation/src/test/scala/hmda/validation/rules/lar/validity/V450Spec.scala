package hmda.validation.rules.lar.validity

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.{ BadValueUtils, LarEditCheckSpec }
import org.scalacheck.Gen

class V450Spec extends LarEditCheckSpec with BadValueUtils {
  property("Applicant ethnicity must = 1,2,3, or 4") {
    forAll(larGen) { lar =>
      lar.mustPass
    }
  }

  val badEthnicityGen: Gen[Int] = intOutsideRange(1, 4)

  property("Applicant ethnicity other than 1,2,3,4 is invalid") {
    forAll(larGen, badEthnicityGen) { (lar, x) =>
      val invalidApplicant = lar.applicant.copy(ethnicity = x)
      val invalidLar = lar.copy(applicant = invalidApplicant)
      invalidLar.mustFail
    }
  }

  override def check: EditCheck[LoanApplicationRegister] = V450
}
