package hmda.validation.rules.lar.validity

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.dsl.{ Result, Success }
import hmda.validation.rules.EditCheck

object V262 extends EditCheck[LoanApplicationRegister] {
  def apply(lar: LoanApplicationRegister): Result = {
    val applicationDate = lar.loan.applicationDate
    if (applicationDate == "NA") {
      lar.actionTakenType is equalTo(6)
    } else {
      Success()
    }
  }

  def name = "V262"
}
