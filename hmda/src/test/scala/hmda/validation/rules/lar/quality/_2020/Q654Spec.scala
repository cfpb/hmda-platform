package hmda.validation.rules.lar.quality._2020

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.rules.EditCheck
import hmda.model.filing.lar.enums._
import hmda.validation.rules.lar.LarEditCheckSpec

class Q654Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = Q654

  property(
    "DTI should be between 0 and 80 or NA/Exempt") {
    forAll(larGen) { lar =>
      lar.copy(income = "NA").mustPass
      lar.copy(income = "5").mustPass
      whenever(List(LoanOriginated, ApplicationApprovedButNotAccepted, PreapprovalRequestApprovedButNotAccepted).contains(lar.action.actionTakenType)){
        val relevantLar = lar.copy(income = "6")
        relevantLar.copy(loan = relevantLar.loan.copy(debtToIncomeRatio = "-.01")).mustFail
        relevantLar.copy(loan = relevantLar.loan.copy(debtToIncomeRatio = "0")).mustPass
        relevantLar.copy(loan = relevantLar.loan.copy(debtToIncomeRatio = "80")).mustPass
        relevantLar.copy(loan = relevantLar.loan.copy(debtToIncomeRatio = "80.01")).mustFail
        relevantLar.copy(loan = relevantLar.loan.copy(debtToIncomeRatio = "NA")).mustPass
        relevantLar.copy(loan = relevantLar.loan.copy(debtToIncomeRatio = "Exempt")).mustPass
      } 
    }
  }

  property("Must pass if not relevant action taken") {
    forAll(larGen) { lar =>
      whenever(!(List(LoanOriginated, ApplicationApprovedButNotAccepted, PreapprovalRequestApprovedButNotAccepted).contains(lar.action.actionTakenType))){
        lar.mustPass
      }
    }
  }

}