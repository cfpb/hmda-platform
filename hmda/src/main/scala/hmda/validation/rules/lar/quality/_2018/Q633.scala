package hmda.validation.rules.lar.quality._2018

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums._
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

object Q633 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "Q633"

  val results = List(ReferWithCaution, Accept, Ineligible, Refer, Eligible, UnableToDetermineOrUnknown, OtherAutomatedUnderwritingResult)

  override def apply(lar: LoanApplicationRegister): ValidationResult =
    when(lar.AUS.aus1 is equalTo(GuaranteedUnderwritingSystem)) {
      lar.ausResult.ausResult1 is containedIn(results)
    } and when(lar.AUS.aus2 is equalTo(GuaranteedUnderwritingSystem)) {
      lar.ausResult.ausResult2 is containedIn(results)
    } and when(lar.AUS.aus3 is equalTo(GuaranteedUnderwritingSystem)) {
      lar.ausResult.ausResult3 is containedIn(results)
    } and when(lar.AUS.aus4 is equalTo(GuaranteedUnderwritingSystem)) {
      lar.ausResult.ausResult4 is containedIn(results)
    } and when(lar.AUS.aus5 is equalTo(GuaranteedUnderwritingSystem)) {
      lar.ausResult.ausResult5 is containedIn(results)
    }
}
