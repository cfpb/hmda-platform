package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums.{ AUSExempt, AUSNotApplicable, EmptyAUSValue, InvalidAutomatedUnderwritingSystemCode, InternalProprietarySystem }
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

object V696_1 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "V696-1"

  override def parent: String = "V696"

  val invalidAUSSystem = new InvalidAutomatedUnderwritingSystemCode
  val ausOtherList =
    List(AUSNotApplicable, AUSExempt, invalidAUSSystem, InternalProprietarySystem)

  override def apply(lar: LoanApplicationRegister): ValidationResult =
    lar.AUS.aus1 not oneOf(EmptyAUSValue, invalidAUSSystem, InternalProprietarySystem) and
      (lar.AUS.aus2 not containedIn(ausOtherList)) and
      (lar.AUS.aus3 not containedIn(ausOtherList)) and
      (lar.AUS.aus4 not containedIn(ausOtherList)) and
      (lar.AUS.aus5 not containedIn(ausOtherList))
}
