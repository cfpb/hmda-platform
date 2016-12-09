package hmda.validation.rules.lar.quality

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.model.fi.lar.fields.LarTopLevelFields._
import hmda.validation.dsl.Result
import hmda.validation.rules.EditCheck
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._

object Q027 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "Q027"

  override def apply(lar: LoanApplicationRegister): Result = {
    when(lar.actionTakenType is oneOf(1, 2, 3, 4, 5, 7, 8)
      and (lar.loan.propertyType is oneOf(1, 2))) {
      lar.applicant.income not equalTo("NA")
    }
  }

  override def fields(lar: LoanApplicationRegister) = Map(
    noField -> ""
  )

}
