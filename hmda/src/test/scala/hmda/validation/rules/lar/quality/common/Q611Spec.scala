package hmda.validation.rules.lar.quality.common

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums._
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class Q611Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = Q611

  property("HOEPA status should be valid") {
    forAll(larGen) { lar =>
      whenever(
        lar.action.actionTakenType != LoanOriginated || lar.lienStatus != SecuredBySubordinateLien || lar.loan.rateSpread == "NA") {
        lar.mustPass
      }

      val lowRs = lar.copy(action =
                             lar.action.copy(actionTakenType = LoanOriginated),
                           lienStatus = SecuredBySubordinateLien,
                           loan = lar.loan.copy(rateSpread = "8.0"))
      lowRs.mustPass

      val appLar = lar.copy(action =
                              lar.action.copy(actionTakenType = LoanOriginated),
                            lienStatus = SecuredBySubordinateLien,
                            loan = lar.loan.copy(rateSpread = "9.0"))
      val validLar = appLar.copy(hoepaStatus = HighCostMortgage)
      validLar.mustPass

      val invalidLar = appLar.copy(hoepaStatus = new InvalidHoepaStatusCode)
      invalidLar.mustFail
    }
  }
}
