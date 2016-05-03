package hmda.validation.rules.lar.validity

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.dsl.Result
import hmda.validation.rules.EditCheck

object V460 extends EditCheck[LoanApplicationRegister] {

  import hmda.validation.dsl.PredicateDefaults._
  import hmda.validation.dsl.PredicateSyntax._

  override def name: String = "V460"

  override def apply(lar: LoanApplicationRegister): Result = {
    lar.applicant.coEthnicity is containedIn(1 to 5)
  }

}
