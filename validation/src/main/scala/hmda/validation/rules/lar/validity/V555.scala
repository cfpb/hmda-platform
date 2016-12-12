package hmda.validation.rules.lar.validity

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.model.fi.lar.fields.LarTopLevelFields._
import hmda.validation.dsl.Result
import hmda.validation.rules.EditCheck
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._

object V555 extends EditCheck[LoanApplicationRegister] {

  val loanPurposeList = List(1, 3)
  val lienStatusList = List(1, 2, 4)

  override def apply(lar: LoanApplicationRegister): Result = {
    when(lar.loan.purpose is containedIn(loanPurposeList)) {
      lar.lienStatus is containedIn(lienStatusList)
    }
  }

  override def name = "V555"

}
