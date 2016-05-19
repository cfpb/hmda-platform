package hmda.validation.rules.lar.validity

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.dsl.Result
import hmda.validation.rules.EditCheck

object V385 extends EditCheck[LoanApplicationRegister] {

  val requiredDenialReasons = (1 to 9).map(_.toString()).toList
  val validDenialReasons = List("") ::: requiredDenialReasons
  val okActionTaken = List(3, 7)

  def apply(lar: LoanApplicationRegister): Result = {
    import hmda.validation.dsl.PredicateCommon._
    import hmda.validation.dsl.PredicateSyntax._

    val denialReasons = List(lar.denial.reason1, lar.denial.reason2, lar.denial.reason3)

    denialReasons.foreach(_ is containedIn(validDenialReasons)) and
      when((lar.agencyCode is equalTo(1)) and (lar.actionTakenType is containedIn(okActionTaken))) {
        denialReasons.intersect(requiredDenialReasons).length is greaterThan(0)
      }
  }

  override def name: String = "V385"
}
