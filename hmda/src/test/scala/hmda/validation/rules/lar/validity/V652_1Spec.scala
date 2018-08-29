package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import org.scalacheck.Gen
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class V652_1Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = V652_1

  property("Co-Applicant age must be greater than 0") {
    forAll(larGen, Gen.choose(-100, 0)) { (lar, a) =>
      lar.mustPass
      val invalidLar = lar.copy(coApplicant = lar.coApplicant.copy(age = a))
      invalidLar.mustFail
    }
  }
}
