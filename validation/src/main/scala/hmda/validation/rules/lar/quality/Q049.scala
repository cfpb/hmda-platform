package hmda.validation.rules.lar.quality

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.model.fi.lar.fields.LarTopLevelFields._
import hmda.validation.dsl.Result
import hmda.validation.rules.EditCheck
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._

object Q049 extends EditCheck[LoanApplicationRegister] {

  override def apply(lar: LoanApplicationRegister): Result = {
    when(lar.actionTakenType is containedIn(List(7, 8))) {
      (lar.geography.msa is equalTo("NA")) and
        (lar.geography.state is equalTo("NA")) and
        (lar.geography.county is equalTo("NA")) and
        (lar.geography.tract is equalTo("NA"))
    }
  }

  override def name: String = "Q049"

  override def description = ""

  override def fields(lar: LoanApplicationRegister) = Map(
    noField -> ""
  )

}
