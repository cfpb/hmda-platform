package hmda.validation.rules.lar.validity

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.dsl.Result
import hmda.validation.rules.EditCheck

object V415 extends EditCheck[LoanApplicationRegister] {

  val preApprovalTypes = List(1, 2, 3)

  def apply(lar: LoanApplicationRegister): Result = {
    lar.preapprovals is containedIn(preApprovalTypes)
  }

  def name = "V415"
}