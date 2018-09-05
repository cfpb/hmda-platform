package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec
import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.enums._

class V656_2Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = V656_2

  property("If Loan not Made Purchaser Not Applicable") {
    forAll(larGen) { lar =>
      val relevantActionTaken = List(
        ApplicationApprovedButNotAccepted,
        ApplicationDenied,
        ApplicationWithdrawnByApplicant,
        FileClosedForIncompleteness,
        PreapprovalRequestDenied,
        PreapprovalRequestApprovedButNotAccepted
      )

      whenever(relevantActionTaken contains lar.action.actionTakenType) {
        lar
          .copy(purchaserType = FarmerMac)
          .mustFail
      }

      lar
        .copy(action = lar.action.copy(actionTakenType = LoanOriginated))
        .mustPass

      lar.copy(purchaserType = PurchaserTypeNotApplicable).mustPass
    }
  }
}
