package hmda.validation.rules.lar.validity

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.model.fi.lar.fields.LarTopLevelFields._
import hmda.validation.dsl.Result
import hmda.validation.rules.EditCheck
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._

object V385 extends EditCheck[LoanApplicationRegister] {

  val requiredDenialReasons = (1 to 9).map(_.toString()).toList
  val validDenialReasons = "" :: requiredDenialReasons
  val okActionTaken = List(3, 7)

  override def apply(lar: LoanApplicationRegister): Result = {
    val denialReasons = List(lar.denial.reason1, lar.denial.reason2, lar.denial.reason3)

    val denialsReasonsAreValid = denialReasons.forall(validDenialReasons.contains(_)) is equalTo(true)

    val atLeastOneDenialIfRequired =
      when((lar.agencyCode is equalTo(1)) and (lar.actionTakenType is containedIn(okActionTaken))) {
        denialReasons.intersect(requiredDenialReasons).length is greaterThan(0)
      }

    denialsReasonsAreValid and atLeastOneDenialIfRequired
  }

  override def name: String = "V385"

  override def description = ""

  override def fields(lar: LoanApplicationRegister) = Map(
    noField -> ""
  )
}
