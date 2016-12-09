package hmda.validation.rules.lar.validity

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.model.fi.lar.fields.LarTopLevelFields._
import hmda.validation.dsl.Result
import hmda.validation.rules.EditCheck
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._

object V425 extends EditCheck[LoanApplicationRegister] {

  override def apply(lar: LoanApplicationRegister): Result = {
    when((lar.loan.purpose is equalTo(1)) and (lar.actionTakenType is equalTo(6))) {
      (lar.preapprovals is equalTo(3))
    }
  }

  override def name = "V425"

  override def fields(lar: LoanApplicationRegister) = Map(
    noField -> ""
  )

}
