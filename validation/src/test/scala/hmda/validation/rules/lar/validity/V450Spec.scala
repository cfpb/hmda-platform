package hmda.validation.rules.lar.validity

import hmda.validation.dsl.{ Failure, Success }
import hmda.validation.rules.lar.LarEditCheckSpec
import org.scalacheck.Gen

class V450Spec extends LarEditCheckSpec with BadValueUtils {
  property("Applicant ethnicity must = 1,2,3, or 4") {
    forAll(larGen) { lar =>
      V450(lar) mustBe Success()
    }
  }

  val badEthnicityGen: Gen[Int] = intOutsideRange(1, 4)

  property("Applicant ethnicity other than 1,2,3,4 is invalid") {
    forAll(larGen, badEthnicityGen) { (lar, x) =>
      val invalidApplicant = lar.applicant.copy(ethnicity = x)
      val invalidLar = lar.copy(applicant = invalidApplicant)
      V450(invalidLar) mustBe a[Failure]
    }
  }

}
