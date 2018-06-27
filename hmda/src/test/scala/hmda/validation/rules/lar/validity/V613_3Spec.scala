package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.{LarAction, LoanApplicationRegister}
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec
import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.enums._

class V613_3Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = V613_3

  property("If Action Taken equals 3,4,5 or 6, then Preapproval must equal 2") {
    forAll(larGen) { lar =>
      val larAction1 = LarAction(PreapprovalNotRequested, ApplicationDenied)
      val larAction2 =
        LarAction(PreapprovalNotRequested, ApplicationWithdrawnByApplicant)
      val larAction3 =
        LarAction(PreapprovalNotRequested, FileClosedForIncompleteness)
      val larAction4 = LarAction(PreapprovalNotRequested, PurchasedLoan)

      lar.copy(action = larAction1).mustPass
      lar.copy(action = larAction2).mustPass
      lar.copy(action = larAction3).mustPass
      lar.copy(action = larAction4).mustPass
    }
  }

  property("Fail if Action Taken equals 3,4,5, or 6 and Preapproval is not 2") {
    forAll(larGen) { lar =>
      whenever(lar.action.preapproval != PreapprovalNotRequested) {
        val larAction1 = LarAction(actionTakenType = ApplicationDenied)
        val larAction2 =
          LarAction(actionTakenType = ApplicationWithdrawnByApplicant)
        val larAction3 =
          LarAction(actionTakenType = FileClosedForIncompleteness)
        val larAction4 = LarAction(actionTakenType = PurchasedLoan)

        lar.copy(action = larAction1).mustFail
        lar.copy(action = larAction2).mustFail
        lar.copy(action = larAction3).mustFail
        lar.copy(action = larAction4).mustFail

        lar.copy(action = LarAction(actionTakenType = LoanOriginated)).mustPass
      }
    }
  }
}
