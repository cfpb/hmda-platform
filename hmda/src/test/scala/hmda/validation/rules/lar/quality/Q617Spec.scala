package hmda.validation.rules.lar.quality

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class Q617Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = Q617

  property(
    "Combined loan-to-value ratio should be greater than calculated loan-to-value ratio") {
    forAll(larGen) { lar =>
      whenever(
        lar.property.propertyValue == "NA" || lar.loan.combinedLoanToValueRatio == "NA") {
        lar.mustPass
      }

      val appLar =
        lar.copy(loan =
          lar.loan.copy(combinedLoanToValueRatio = "50.0", amount = 10.0))
      appLar
        .copy(property = appLar.property.copy(propertyValue = "19.0"))
        .mustFail
      appLar
        .copy(property = appLar.property.copy(propertyValue = "20.0"))
        .mustFail
      appLar
        .copy(property = appLar.property.copy(propertyValue = "21.0"))
        .mustPass
    }
  }
}
