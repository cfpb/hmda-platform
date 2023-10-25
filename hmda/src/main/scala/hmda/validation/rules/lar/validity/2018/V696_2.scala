package hmda.validation.rules.lar.validity._2018

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums._
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

object V696_2 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "V696-2"

  override def parent: String = "V696"

  val invalidAUSResult = new InvalidAutomatedUnderwritingResultCode
  val ausOtherList = List(AutomatedUnderwritingResultNotApplicable, AUSResultExempt, invalidAUSResult)

  override def apply(lar: LoanApplicationRegister): ValidationResult =
    lar.ausResult.ausResult1 not oneOf(EmptyAUSResultValue, invalidAUSResult) and
      (lar.ausResult.ausResult2 not containedIn(ausOtherList)) and
      (lar.ausResult.ausResult3 not containedIn(ausOtherList)) and
      (lar.ausResult.ausResult4 not containedIn(ausOtherList)) and
      (lar.ausResult.ausResult5 not containedIn(ausOtherList))
}
