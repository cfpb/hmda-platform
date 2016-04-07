package hmda.validation.rules.lar.validity

import hmda.validation.dsl.{ Failure, Success }
import hmda.validation.rules.lar.LarEditCheckSpec
import org.scalacheck.Gen

class V310Spec extends LarEditCheckSpec {

  property("Succeeds when Applicant Race1 is 1,2,3,4,5,6,7") {
    forAll(larGen) { lar =>
      V310(lar) mustBe Success()
    }
  }

  val badApplicantRaceGen: Gen[Int] = {
    val belowRange = Gen.choose(Integer.MIN_VALUE, 0)
    val aboveRange = Gen.choose(8, Integer.MAX_VALUE)
    Gen.oneOf(belowRange, aboveRange)
  }

  property("Fails when Applicant Race1 is < 1 or > 7") {
    forAll(larGen, badApplicantRaceGen) { (lar, r1) =>
      val invalidApplicant = lar.applicant.copy(race1 = r1)
      val invalidLar = lar.copy(applicant = invalidApplicant)
      V310(invalidLar) mustBe a[Failure]
    }
  }

}
