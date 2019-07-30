package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums.{
  ManufacturedHomeLandNotApplicable,
  UnpaidLeasehold
}
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class V690_2Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = V690_2

  property(
    "If there are multi family units, manufactured interest must be exempt or NA") {
    forAll(larGen) { lar =>
      whenever(lar.property.multiFamilyAffordableUnits == "NA") {
        lar.mustPass
      }

      val appLar =
        lar.copy(property = lar.property.copy(multiFamilyAffordableUnits = "1"))
      appLar
        .copy(
          property = appLar.property.copy(manufacturedHomeLandPropertyInterest =
            ManufacturedHomeLandNotApplicable))
        .mustPass
      appLar
        .copy(
          property = appLar.property.copy(
            manufacturedHomeLandPropertyInterest = UnpaidLeasehold))
        .mustFail
    }
  }
}
