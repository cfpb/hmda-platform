package hmda.validation.rules.lar.validity

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.dsl.Result
import hmda.validation.rules.EditCheck
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._

object V355 extends EditCheck[LoanApplicationRegister] {

  override def apply(lar: LoanApplicationRegister): Result = {
    val denialReasons = List(lar.denial.reason1, lar.denial.reason2, lar.denial.reason3)

    val validDenialReason = "" :: (1 to 9).map(_.toString()).toList

    val agencyCodeValid = lar.agencyCode is containedIn(List(2, 3, 5, 7, 9))
    val agencyCodeAndActionTakenValid = (lar.agencyCode is equalTo(1)) and (lar.actionTakenType is containedIn(List(3, 7)))

    when(agencyCodeValid or agencyCodeAndActionTakenValid) {
      denialReasons.forall(validDenialReason.contains(_)) is equalTo(true)
    }
  }

  override def name: String = "V355"
}
