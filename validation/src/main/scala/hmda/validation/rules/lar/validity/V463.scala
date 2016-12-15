package hmda.validation.rules.lar.validity

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.model.fi.lar.fields.LarTopLevelFields._
import hmda.validation.dsl.Result
import hmda.validation.rules.EditCheck
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._

object V463 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "V463"

  override def description = ""

  override def fields(lar: LoanApplicationRegister) = Map(
    noField -> ""
  )

  override def apply(lar: LoanApplicationRegister): Result = {
    when(lar.applicant.coEthnicity is equalTo(5)) {
      (lar.applicant.coRace1 is equalTo(8)) and
        (lar.applicant.coSex is equalTo(5))
    }
  }
}
