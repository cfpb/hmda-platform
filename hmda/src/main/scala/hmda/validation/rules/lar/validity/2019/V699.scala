package hmda.validation.rules.lar.validity._2019

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._

object V699 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "V699"

  override def apply(lar: LoanApplicationRegister): ValidationResult = {
    val validAUSResults = List(
      ApproveEligible,
      ApproveIneligible,
      ReferEligilbe,
      ReferIneligible,
      ReferWithCaution,
      OutOfScope,
      Error,
      Accept,
      Caution,
      Ineligible,
      Incomplete,
      Invalid,
      Refer,
      Eligible,
      UnableToDetermineOrUnknown,
      OtherAutomatedUnderwritingResult,
      AcceptEligible,
      AcceptIneligible,
      AcceptUnableToDetermine,
      ReferWithCautionEligible,
      ReferWithCautionIneligible,
      ReferUnableToDetermine,
      ReferWithCautionUnableToDetermine
    )
    when(lar.AUS.aus1 is equalTo(OtherAUS)) {
      lar.ausResult.ausResult1 is containedIn(validAUSResults)
    } and
      when(lar.AUS.aus2 is equalTo(OtherAUS)) {
        lar.ausResult.ausResult2 is containedIn(validAUSResults)
      } and
      when(lar.AUS.aus3 is equalTo(OtherAUS)) {
        lar.ausResult.ausResult3 is containedIn(validAUSResults)
      } and
      when(lar.AUS.aus4 is equalTo(OtherAUS)) {
        lar.ausResult.ausResult4 is containedIn(validAUSResults)
      } and
      when(lar.AUS.aus5 is equalTo(OtherAUS)) {
        lar.ausResult.ausResult5 is containedIn(validAUSResults)
      }
  }
}
