package hmda.validation.rules.lar.validity

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.{ BadValueUtils, LarEditCheckSpec }
import org.scalacheck.Gen

class V325Spec extends LarEditCheckSpec with BadValueUtils {

  property("CoApplicant sex must = 1,2,3,4, or 5") {
    forAll(larGen) { lar =>
      lar.mustPass
    }
  }

  val badApplicantCoSexGen: Gen[Int] = intOutsideRange(1, 5)

  property("CoApplicant sex other than 1,2,3,4,5 is invalid") {
    forAll(larGen, badApplicantCoSexGen) { (lar, sex) =>
      val invalidApplicant = lar.applicant.copy(coSex = sex)
      val invalidLar = lar.copy(applicant = invalidApplicant)
      invalidLar.mustFail
    }
  }

  override def check: EditCheck[LoanApplicationRegister] = V325
}
