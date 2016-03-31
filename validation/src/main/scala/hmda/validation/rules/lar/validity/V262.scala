package hmda.validation.rules.lar.validity

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.dsl.{ CommonDsl, Result }

object V262 extends CommonDsl {
  def apply(lar: LoanApplicationRegister): Result = {
    val applicationDate = lar.loan.applicationDate
    when(applicationDate is equalTo("NA")) {
      lar.actionTakenType is equalTo(6)
    }
  }
}
