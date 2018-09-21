package hmda.validation.rules.lar.validity

import com.typesafe.config.ConfigFactory
import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class V692_3Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = V692_3

  property(
    "If total units is greater than or equal to 5, multifamily affordable units must be valid") {
    forAll(larGen) { lar =>
      val config = ConfigFactory.load()
      val units = config.getInt("edits.V692.units")

      val unappLar =
        lar.copy(property = lar.property.copy(totalUnits = units - 1))
      unappLar.mustPass

      val appLar = lar.copy(property = lar.property.copy(totalUnits = units))
      val invalidLar1 = appLar.copy(property =
        appLar.property.copy(multiFamilyAffordableUnits = (units + 1).toString))
      invalidLar1.mustFail

      val invalidLar2 = appLar.copy(
        property = appLar.property.copy(multiFamilyAffordableUnits = "test"))
      invalidLar2.mustFail

      val validLar1 = appLar.copy(
        property = appLar.property.copy(multiFamilyAffordableUnits = "NA"))
      validLar1.mustPass

      val validLar2 = appLar.copy(
        property =
          appLar.property.copy(multiFamilyAffordableUnits = units.toString))
      validLar2.mustPass
    }
  }
}
