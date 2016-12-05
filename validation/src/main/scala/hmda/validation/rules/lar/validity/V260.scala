package hmda.validation.rules.lar.validity

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.dsl.Result
import hmda.validation.rules.EditCheck
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._

object V260 extends EditCheck[LoanApplicationRegister] {

  val denialReasons = (1 to 9).map(_.toString)

  override def apply(lar: LoanApplicationRegister): Result = {
    when((lar.denial.reason1 is containedIn(denialReasons))
      or (lar.denial.reason2 is containedIn(denialReasons))
      or (lar.denial.reason3 is containedIn(denialReasons))) {
      lar.actionTakenType is oneOf(3, 7)
    }
  }

  override def name = "V260"

  override def description = ""
}
