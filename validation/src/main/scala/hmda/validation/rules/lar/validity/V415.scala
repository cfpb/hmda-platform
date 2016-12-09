package hmda.validation.rules.lar.validity

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.model.fi.lar.fields.LarTopLevelFields._
import hmda.validation.dsl.Result
import hmda.validation.rules.EditCheck
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._

object V415 extends EditCheck[LoanApplicationRegister] {

  val preApprovalList = List(1, 2, 3)

  override def apply(lar: LoanApplicationRegister): Result = {
    lar.preapprovals is containedIn(preApprovalList)
  }

  override def name = "V415"

  override def fields(lar: LoanApplicationRegister) = Map(
    noField -> ""
  )

}
