package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums.{
  ApplicationWithdrawnByApplicant,
  LoanOriginated
}
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class V688_2Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = V688_2

  property(
    "If application withdrawn or incomplete, property value must be Exempt or NA") {
    forAll(larGen) { lar =>
      val unappLar =
        lar.copy(action = lar.action.copy(actionTakenType = LoanOriginated))
      unappLar.mustPass

      val appLar = lar.copy(
        action =
          lar.action.copy(actionTakenType = ApplicationWithdrawnByApplicant))
      appLar
        .copy(property = appLar.property.copy(propertyValue = "NA"))
        .mustPass
      appLar
        .copy(property = appLar.property.copy(propertyValue = "Exempt"))
        .mustPass
      appLar
        .copy(property = appLar.property.copy(propertyValue = "1.0"))
        .mustFail
    }
  }
}
