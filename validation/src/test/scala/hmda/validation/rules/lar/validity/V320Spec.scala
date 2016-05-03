package hmda.validation.rules.lar.validity

import hmda.validation.dsl.{ Failure, Success }
import hmda.validation.rules.lar.LarEditCheckSpec
import org.scalacheck.Gen

class V320Spec extends LarEditCheckSpec with BadValueUtils {
  property("Applicant sex must = 1,2,3, or 4") {
    forAll(larGen) { lar =>
      V320(lar) mustBe Success()
    }
  }

  val badApplicantSexGen: Gen[Int] = intOutsideRange(1, 4)

  property("Applicant sex other than 1,2,3,4 is invalid") {
    forAll(larGen, badApplicantSexGen) { (lar, sex) =>
      val invalidApplicant = lar.applicant.copy(sex = sex)
      val invalidLar = lar.copy(applicant = invalidApplicant)
      V320(invalidLar) mustBe a[Failure]
    }
  }

}
