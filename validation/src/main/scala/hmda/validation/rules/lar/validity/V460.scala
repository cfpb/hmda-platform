package hmda.validation.rules.lar.validity

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.model.fi.lar.fields.LarTopLevelFields._
import hmda.validation.dsl.Result
import hmda.validation.rules.EditCheck
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._

object V460 extends EditCheck[LoanApplicationRegister] {

  override def name: String = "V460"

  override def fields(lar: LoanApplicationRegister) = Map(
    noField -> ""
  )

  override def apply(lar: LoanApplicationRegister): Result = {
    lar.applicant.coEthnicity is containedIn(1 to 5)
  }

}
