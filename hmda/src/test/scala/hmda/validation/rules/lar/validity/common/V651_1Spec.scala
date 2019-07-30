package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import org.scalacheck.Gen
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class V651_1Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = V651_1

  property("Applicant age must be valid") {
    forAll(larGen, Gen.choose(-100, 0)) { (lar, a) =>
      lar.mustPass
      val invalidLar = lar.copy(applicant = lar.applicant.copy(age = a))
      invalidLar.mustFail
    }
  }
}
