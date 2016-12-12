package hmda.validation.rules.lar.validity

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.model.fi.lar.fields.LarTopLevelFields._
import hmda.validation.dsl.Result
import hmda.validation.rules.EditCheck
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._

object V310 extends EditCheck[LoanApplicationRegister] {

  override def apply(lar: LoanApplicationRegister): Result = {
    lar.applicant.race1 is containedIn(1 to 7)
  }

  override def name: String = "V310"

}
