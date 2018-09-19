package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class V679_3Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = V679_3

  property("Debt to income ratio must be valid") {
    forAll(larGen) { lar =>
      val unappLar = lar.copy(
        property = lar.property.copy(multiFamilyAffordableUnits = "NA"))
      unappLar.mustPass

      val appLar =
        lar.copy(property = lar.property.copy(multiFamilyAffordableUnits = "1"))

      appLar.copy(loan = appLar.loan.copy(debtToIncomeRatio = "test")).mustFail
      appLar.copy(loan = appLar.loan.copy(debtToIncomeRatio = "")).mustFail
      appLar.copy(loan = appLar.loan.copy(debtToIncomeRatio = "-1.0")).mustFail
      appLar.copy(loan = appLar.loan.copy(debtToIncomeRatio = "NA")).mustPass
    }
  }
}
