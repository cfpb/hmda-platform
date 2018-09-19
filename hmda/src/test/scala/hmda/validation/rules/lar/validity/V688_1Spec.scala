package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class V688_1Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = V688_1

  property("Property value must be valid") {
    forAll(larGen) { lar =>
      lar.mustPass

      lar.copy(property = lar.property.copy(propertyValue = "Exempt")).mustPass
      lar.copy(property = lar.property.copy(propertyValue = "test")).mustFail
      lar.copy(property = lar.property.copy(propertyValue = "-1.0")).mustFail
    }
  }
}
