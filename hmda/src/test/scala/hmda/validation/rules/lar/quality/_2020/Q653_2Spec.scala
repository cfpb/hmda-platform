package hmda.validation.rules.lar.quality._2020

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.rules.EditCheck
import hmda.model.filing.lar.enums._
import hmda.validation.rules.lar.LarEditCheckSpec

class Q653_2Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = Q653_2

  property(
    "CLTV should be between 0 and 1000") {
    forAll(larGen) { lar =>
      whenever(List(ApplicationDenied, ApplicationWithdrawnByApplicant, FileClosedForIncompleteness, PurchasedLoan, PreapprovalRequestDenied).contains(lar.action.actionTakenType)){
        val relevantLar = lar.copy(income = "6")
        relevantLar.copy(loan = relevantLar.loan.copy(combinedLoanToValueRatio = "-.01")).mustFail
        relevantLar.copy(loan = relevantLar.loan.copy(combinedLoanToValueRatio = "0")).mustPass
        relevantLar.copy(loan = relevantLar.loan.copy(combinedLoanToValueRatio = "1000")).mustPass
        relevantLar.copy(loan = relevantLar.loan.copy(combinedLoanToValueRatio = "1000.01")).mustFail
        relevantLar.copy(loan = relevantLar.loan.copy(combinedLoanToValueRatio = "NA")).mustPass
        relevantLar.copy(loan = relevantLar.loan.copy(combinedLoanToValueRatio = "Exempt")).mustPass
      } 

    }
  }

  property("Must pass if not relevant action taken") {
    forAll(larGen) { lar =>
      whenever(!(List(ApplicationDenied, ApplicationWithdrawnByApplicant, FileClosedForIncompleteness, PurchasedLoan, PreapprovalRequestDenied).contains(lar.action.actionTakenType))){
        lar.mustPass
      }
    }
  }
        
}
