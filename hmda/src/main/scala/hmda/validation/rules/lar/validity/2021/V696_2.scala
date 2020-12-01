package hmda.validation.rules.lar.validity._2021

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck
import hmda.model.filing.lar.enums._

object V696_2 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "V696-2"

  override def parent: String = "V696"

  override def apply(lar: LoanApplicationRegister): ValidationResult = {
    val ausResult1List = 
        List(
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
            AutomatedUnderwritingResultNotApplicable,
            AcceptEligible,
            AcceptIneligible,
            AcceptUnableToDetermine,
            ReferWithCautionEligible,
            ReferWithCautionIneligible,
            ReferUnableToDetermine,
            ReferWithCautionUnableToDetermine,
            AUSResultExempt
        )
    val ausResultOtherList = 
        List(
            EmptyAUSResultValue,
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

    (lar.ausResult.ausResult1 is containedIn(ausResult1List)) and (lar.ausResult.ausResult2 is containedIn(ausResultOtherList)) and
    (lar.ausResult.ausResult3 is containedIn(ausResultOtherList)) and (lar.ausResult.ausResult4 is containedIn(ausResultOtherList)) and
    (lar.ausResult.ausResult5 is containedIn(ausResultOtherList))
  }

}
