package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import org.scalacheck.Gen
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class V691Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = V691

  property("Total units must be greater than 0") {
    forAll(larGen, Gen.choose(-100, 0)) { (lar, t) =>
      lar.mustPass
      val invalidLar = lar.copy(property = lar.property.copy(totalUnits = t))
      invalidLar.mustFail
    }
  }
}
