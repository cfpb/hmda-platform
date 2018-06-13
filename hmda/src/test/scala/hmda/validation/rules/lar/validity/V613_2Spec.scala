package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.{LarAction, LoanApplicationRegister}
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec
import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.enums.{
  LoanOriginated,
  PreapprovalRequestApprovedButNotAccepted,
  PreapprovalRequestDenied,
  PreapprovalRequested
}

class V613_2Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = V613_2

  property("If Action Taken equals 7 or 8, then Preapproval must equal 1") {
    forAll(larGen) { lar =>
      val larAction1 = LarAction(PreapprovalRequested,
                                 PreapprovalRequestApprovedButNotAccepted)
      val larAction2 = LarAction(PreapprovalRequested, PreapprovalRequestDenied)
      lar.copy(action = larAction1).mustPass
      lar.copy(action = larAction2).mustPass
    }
  }

  property("Fail if Action Taken equals 7 or 8, and Preapproval is not 1") {
    forAll(larGen) { lar =>
      whenever(lar.action.preapproval != PreapprovalRequested) {
        val larAction1 =
          LarAction(actionTakenType = PreapprovalRequestApprovedButNotAccepted)
        val larAction2 = LarAction(actionTakenType = PreapprovalRequestDenied)

        lar.copy(action = larAction1).mustFail
        lar.copy(action = larAction2).mustFail

        lar.copy(action = LarAction(actionTakenType = LoanOriginated)).mustPass
      }
    }
  }

}
