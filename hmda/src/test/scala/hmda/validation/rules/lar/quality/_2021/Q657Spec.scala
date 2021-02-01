package hmda.validation.rules.lar.quality._2021

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class Q657Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = Q657

  property("Non-exempt numeric field should not have a value of 1111") {
    forAll(larGen) { lar =>
      val relevantLar1 = lar.copy(property = lar.property.copy(totalUnits = 5))

      relevantLar1.copy(property = relevantLar1.property.copy(multiFamilyAffordableUnits = "Exempt")).mustFail


    }
  }
}
