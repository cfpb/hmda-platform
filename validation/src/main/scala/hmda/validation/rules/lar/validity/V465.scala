package hmda.validation.rules.lar.validity

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.model.fi.lar.fields.LarTopLevelFields._
import hmda.validation.dsl.Result
import hmda.validation.rules.EditCheck
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._

object V465 extends EditCheck[LoanApplicationRegister] {

  override def apply(lar: LoanApplicationRegister): Result = {
    when(lar.applicant.coEthnicity is oneOf(1, 2, 3)) {
      lar.applicant.coRace1 not oneOf(7, 8)
    }
  }

  override def name: String = "V465"

  override def fields(lar: LoanApplicationRegister) = Map(
    noField -> ""
  )

}
