package hmda.validation.rules.lar.quality.common

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class Q601Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = Q601

  property("Application date must be within 2 years of action taken date") {
    forAll(larGen) { lar =>
      whenever(lar.loan.applicationDate == "NA") {
        lar.mustPass
      }

      val minYear = lar.action.actionTakenDate.toString.slice(0, 4).toInt - 2
      val minMonthDay = lar.action.actionTakenDate.toString.slice(4, 8)

      val goodMonthDay = (lar.action.actionTakenDate + 1).toString.slice(4, 8)
      val badMonthDay = (lar.action.actionTakenDate - 1).toString.slice(4, 8)

      val minDate = s"$minYear$minMonthDay"
      val goodDate = s"$minYear$goodMonthDay"
      val badDate = s"$minYear$badMonthDay"

      lar.copy(loan = lar.loan.copy(applicationDate = minDate)).mustPass
      lar.copy(loan = lar.loan.copy(applicationDate = goodDate)).mustPass
      lar.copy(loan = lar.loan.copy(applicationDate = badDate)).mustFail
    }
  }
}
