package hmda.validation.rules.lar.validity

import hmda.validation.dsl.{ Failure, Success }
import hmda.validation.rules.lar.LarEditCheckSpec
import org.scalacheck.Gen

class V460Spec extends LarEditCheckSpec {
  property("CoApplicant ethnicity must = 1,2,3,4 or 5") {
    forAll(larGen) { lar =>
      V460(lar) mustBe Success()
    }
  }

  val badCoEthnicityGen: Gen[Int] = {
    val belowRange = Gen.choose(Integer.MIN_VALUE, 0)
    val aboveRange = Gen.choose(6, Integer.MAX_VALUE)
    Gen.oneOf(belowRange, aboveRange)
  }

  property("Applicant ethnicity other than 1,2,3,4,5 is invalid") {
    forAll(larGen, badCoEthnicityGen) { (lar, x) =>
      val invalidApplicant = lar.applicant.copy(coEthnicity = x)
      val invalidLar = lar.copy(applicant = invalidApplicant)
      V460(invalidLar) mustBe a[Failure]
    }
  }
}
