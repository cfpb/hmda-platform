package hmda.validation.rules.lar.validity

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.dsl.Result
import hmda.validation.rules.EditCheck
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._

object V360 extends EditCheck[LoanApplicationRegister] {
  override def apply(lar: LoanApplicationRegister): Result = {
    val denialList = List(lar.denial.reason1, lar.denial.reason2, lar.denial.reason3).filterNot(_.isEmpty)

    denialList.distinct.size is equalTo(denialList.size)
  }

  override def name: String = "V360"
}
