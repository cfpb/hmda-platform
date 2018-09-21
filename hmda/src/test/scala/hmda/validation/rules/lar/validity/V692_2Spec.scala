package hmda.validation.rules.lar.validity

import com.typesafe.config.ConfigFactory
import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class V692_2Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = V692_2

  property(
    "If total units is less than 5, multifamily affordable units must be NA or exempt") {
    forAll(larGen) { lar =>
      val config = ConfigFactory.load()
      val units = config.getInt("edits.V692.units")

      val unappLar = lar.copy(property = lar.property.copy(totalUnits = units))
      unappLar.mustPass

      val appLar =
        lar.copy(property = lar.property.copy(totalUnits = units - 1))
      val invalidLar1 = appLar.copy(
        property = appLar.property.copy(multiFamilyAffordableUnits = "1"))
      invalidLar1.mustFail

      val invalidLar2 = appLar.copy(
        property = appLar.property.copy(multiFamilyAffordableUnits = "test"))
      invalidLar2.mustFail

      val validLar = appLar.copy(
        property = appLar.property.copy(multiFamilyAffordableUnits = "NA"))
      validLar.mustPass
    }
  }
}
