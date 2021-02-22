package hmda.validation.rules.lar.quality._2021

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class Q655Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = Q655

  property("Interest rate should not be greater than 20") {
    forAll(larGen) { lar =>
      val relevantLar1 = lar.copy(property = lar.property.copy(totalUnits = 5))
      val relevantLar2 = lar.copy(property = lar.property.copy(totalUnits = 6))
      val nonrelevantLar = lar.copy(property = lar.property.copy(totalUnits = 4))

      relevantLar1.copy(property = relevantLar1.property.copy(multiFamilyAffordableUnits = "Exempt")).mustPass
      relevantLar1.copy(property = relevantLar1.property.copy(multiFamilyAffordableUnits = "NA")).mustFail
      relevantLar1.copy(property = relevantLar1.property.copy(multiFamilyAffordableUnits = "0")).mustPass
      relevantLar1.copy(property = relevantLar1.property.copy(multiFamilyAffordableUnits = "1")).mustPass
      relevantLar2.copy(property = relevantLar2.property.copy(multiFamilyAffordableUnits = "0")).mustPass
      relevantLar2.copy(property = relevantLar2.property.copy(multiFamilyAffordableUnits = "abc")).mustFail

      nonrelevantLar.mustPass

    }
  }
}
