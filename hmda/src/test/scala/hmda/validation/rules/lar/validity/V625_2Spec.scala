package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class V625_2Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = V625_1

  property("Census Tract must be valid") {
    forAll(larGen) { lar =>
      val unappLar = lar.copy(
        geography = lar.geography.copy(tract = "NA")
      )
      unappLar.mustPass

      val appLar = lar.copy(geography = lar.geography.copy(tract = "1"))
      appLar.mustFail

      val emptyTract = lar.copy(geography = lar.geography.copy(tract = ""))
      emptyTract.mustFail
    }
  }
}
