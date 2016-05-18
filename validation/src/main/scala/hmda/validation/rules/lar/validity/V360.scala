package hmda.validation.rules.lar.validity

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.dsl.Result
import hmda.validation.rules.EditCheck

object V360 extends EditCheck[LoanApplicationRegister] {
  override def apply(lar: LoanApplicationRegister): Result = {
    notEqualIgnoreSpace(lar.denial.reason1, lar.denial.reason2) and
      notEqualIgnoreSpace(lar.denial.reason2, lar.denial.reason3) and
      notEqualIgnoreSpace(lar.denial.reason3, lar.denial.reason1)
  }

  private def notEqualIgnoreSpace(reason1: String, reason2: String): Result = {
    (reason1 not equalTo(reason2)) or (reason1 not numeric)
  }

  override def name: String = "V360"
}