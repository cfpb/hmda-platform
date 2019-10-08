package hmda.validation.rules.lar.quality._2018

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums._
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

object Q643 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "Q643"

  val results = List(ApproveEligible,
                     ApproveIneligible,
                     ReferEligilbe,
                     ReferIneligible,
                     ReferWithCaution,
                     OutOfScope,
                     Error,
                     UnableToDetermineOrUnknown)

  override def apply(lar: LoanApplicationRegister): ValidationResult =
    when(lar.AUS.aus1 is equalTo(DesktopUnderwriter)) {
      lar.ausResult.ausResult1 is containedIn(results)
    } and when(lar.AUS.aus2 is equalTo(DesktopUnderwriter)) {
      lar.ausResult.ausResult2 is containedIn(results)
    } and when(lar.AUS.aus3 is equalTo(DesktopUnderwriter)) {
      lar.ausResult.ausResult3 is containedIn(results)
    } and when(lar.AUS.aus4 is equalTo(DesktopUnderwriter)) {
      lar.ausResult.ausResult4 is containedIn(results)
    } and when(lar.AUS.aus5 is equalTo(DesktopUnderwriter)) {
      lar.ausResult.ausResult5 is containedIn(results)
    }
}
