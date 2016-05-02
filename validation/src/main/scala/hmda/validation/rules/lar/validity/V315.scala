package hmda.validation.rules.lar.validity

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.dsl.Result
import hmda.validation.rules.EditCheck

object V315 extends EditCheck[LoanApplicationRegister] {

  import hmda.validation.dsl.PredicateDefaults._
  import hmda.validation.dsl.PredicateSyntax._

  def apply(lar: LoanApplicationRegister): Result = {
    lar.applicant.coRace1 is containedIn(1 to 8)
  }

  override def name: String = "V315"

}
