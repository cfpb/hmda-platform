package hmda.validation.rules.lar.validity

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.dsl.Result
import hmda.validation.rules.EditCheck

object V475 extends EditCheck[LoanApplicationRegister] {

  import hmda.validation.dsl.PredicateDefaults._
  import hmda.validation.dsl.PredicateSyntax._

  def apply(lar: LoanApplicationRegister): Result = {
    when(lar.applicant.race1 is containedIn(List(6, 7))) {
      (lar.applicant.race2 is equalTo("")) and
        (lar.applicant.race3 is equalTo("")) and
        (lar.applicant.race4 is equalTo("")) and
        (lar.applicant.race5 is equalTo(""))
    }
  }

  def name: String = "V475"

}
