package hmda.validation.rules.lar.validity

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec
import org.scalacheck.Gen

class V320Spec extends LarEditCheckSpec with BadValueUtils {
  property("Applicant sex must = 1,2,3, or 4") {
    forAll(larGen) { lar =>
      lar.mustPass
    }
  }

  val badApplicantSexGen: Gen[Int] = intOutsideRange(1, 4)

  property("Applicant sex other than 1,2,3,4 is invalid") {
    forAll(larGen, badApplicantSexGen) { (lar, sex) =>
      val invalidApplicant = lar.applicant.copy(sex = sex)
      val invalidLar = lar.copy(applicant = invalidApplicant)
      invalidLar.mustFail
    }
  }

  override def check: EditCheck[LoanApplicationRegister] = V320
}
