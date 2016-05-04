package hmda.validation.rules.lar.validity

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.dsl.Result
import hmda.validation.rules.EditCheck

object V325 extends EditCheck[LoanApplicationRegister] {

  import hmda.validation.dsl.PredicateDefaults._
  import hmda.validation.dsl.PredicateSyntax._

  override def apply(lar: LoanApplicationRegister): Result = {
    lar.applicant.coSex is containedIn(1 to 5)
  }

  override def name = "V325"

}
