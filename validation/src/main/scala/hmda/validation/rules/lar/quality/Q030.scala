package hmda.validation.rules.lar.quality

import hmda.census.model.{ Tract, TractLookup }
import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.Result
import hmda.validation.rules.EditCheck

object Q030 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "Q030"

  override def apply(lar: LoanApplicationRegister): Result = {
    val tract = TractLookup.forLar(lar).getOrElse(Tract())

    when(lar.actionTakenType is containedIn(1 to 6)) {
      (lar.geography.tract not "NA") and
        (lar.geography.county not "NA") and
        (lar.geography.state not "NA") and
        ((lar.geography.msa not "NA") or ((lar.geography.msa is "NA") and (tract.msa is "99999")))
    }
  }

}
