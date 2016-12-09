package hmda.validation.rules.lar.validity

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.model.fi.lar.fields.LarTopLevelFields._
import hmda.validation.dsl.Result
import hmda.validation.rules.EditCheck
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._

object V215 extends EditCheck[LoanApplicationRegister] {
  override def apply(lar: LoanApplicationRegister): Result = {
    when(lar.actionTakenType is equalTo(6)) {
      lar.loan.applicationDate is equalTo("NA")
    }
  }

  override def name = "V215"

  override def fields(lar: LoanApplicationRegister) = Map(
    noField -> ""
  )

}
