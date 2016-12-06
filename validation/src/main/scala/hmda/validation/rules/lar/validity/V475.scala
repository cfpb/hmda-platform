package hmda.validation.rules.lar.validity

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.model.fi.lar.fields.LarTopLevelFields._
import hmda.validation.dsl.Result
import hmda.validation.rules.EditCheck
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._

object V475 extends EditCheck[LoanApplicationRegister] {

  override def apply(lar: LoanApplicationRegister): Result = {
    when(lar.applicant.race1 is containedIn(List(6, 7))) {
      (lar.applicant.race2 is equalTo("")) and
        (lar.applicant.race3 is equalTo("")) and
        (lar.applicant.race4 is equalTo("")) and
        (lar.applicant.race5 is equalTo(""))
    }
  }

  override def name: String = "V475"

  override def description = ""

  override def fields(lar: LoanApplicationRegister) = Map(
    noField -> ""
  )

}
