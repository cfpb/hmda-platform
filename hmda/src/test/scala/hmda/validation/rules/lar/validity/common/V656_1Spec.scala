package hmda.validation.rules.lar.validity

import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec
import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar._2018.LoanApplicationRegister
import hmda.model.filing.lar.enums.InvalidPurchaserCode

class V656_1Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = V656_1

  property("Purchaser Code must be Valid") {
    forAll(larGen) { lar =>
      whenever(lar.purchaserType != InvalidPurchaserCode) {
        lar.mustPass
      }
      lar.copy(purchaserType = InvalidPurchaserCode).mustFail
    }
  }
}
