package hmda.validation.rules.validity.lar

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.dsl.{ Success, Result, CommonDsl }

object V262 extends CommonDsl {
  def apply(lar: LoanApplicationRegister): Result = {
    val applicationDate = lar.loan.applicationDate
    if (applicationDate == "NA") {
      lar.actionTakenType is equalTo(6)
    } else {
      Success()
    }
  }
}
