package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec
import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.enums._

class V613_4Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = V613_4

  property("If Preapproval equals 1, then Action Taken must equal 1,2,7 or 8") {
    forAll(larGen) { lar =>
      whenever(lar.action.preapproval == PreapprovalRequested) {
        lar
          .copy(action = lar.action.copy(actionTakenType = LoanOriginated))
          .mustPass
        lar
          .copy(action = lar.action.copy(
            actionTakenType = ApplicationApprovedButNotAccepted))
          .mustPass
        lar
          .copy(action =
            lar.action.copy(actionTakenType = PreapprovalRequestDenied))
          .mustPass
        lar
          .copy(action = lar.action.copy(
            actionTakenType = PreapprovalRequestApprovedButNotAccepted))
          .mustPass
      }
    }
  }

  property("Fail when Preapproval is 1, and Action Taken type not 1,2,7 or 8") {
    forAll(larGen) { lar =>
      whenever(lar.action.preapproval == PreapprovalRequested) {
        whenever(lar.action.actionTakenType != LoanOriginated &&
          lar.action.actionTakenType != ApplicationApprovedButNotAccepted &&
          lar.action.actionTakenType != PreapprovalRequestDenied &&
          lar.action.actionTakenType != PreapprovalRequestApprovedButNotAccepted) {
          lar.mustFail
        }
      }
    }
  }
}
