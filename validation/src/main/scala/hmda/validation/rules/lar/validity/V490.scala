package hmda.validation.rules.lar.validity

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.model.fi.lar.fields.LarTopLevelFields._
import hmda.validation.dsl.Result
import hmda.validation.rules.EditCheck
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._

object V490 extends EditCheck[LoanApplicationRegister] {

  override def apply(lar: LoanApplicationRegister): Result = {
    when(lar.applicant.coRace1 is containedIn(List(6, 7, 8))) {
      (lar.applicant.coRace2 is equalTo("")) and
        (lar.applicant.coRace3 is equalTo("")) and
        (lar.applicant.coRace4 is equalTo("")) and
        (lar.applicant.coRace5 is equalTo(""))
    }
  }

  override def name: String = "V490"

  override def description = ""

  override def fields(lar: LoanApplicationRegister) = Map(
    noField -> ""
  )

}
