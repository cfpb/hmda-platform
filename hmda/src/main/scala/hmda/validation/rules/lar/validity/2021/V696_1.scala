package hmda.validation.rules.lar.validity._2021

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck
import hmda.model.filing.lar.enums._

object V696_1 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "V696-1"

  override def parent: String = "V696"

  override def apply(lar: LoanApplicationRegister): ValidationResult = {
    val aus1List = List(
        DesktopUnderwriter,
        LoanProspector,
        TechnologyOpenToApprovedLenders,
        GuaranteedUnderwritingSystem,
        OtherAUS,
        AUSNotApplicable,
        InternalProprietarySystem,
        AUSExempt
    )
    val ausOtherList = List(
        DesktopUnderwriter,
        LoanProspector,
        TechnologyOpenToApprovedLenders,
        GuaranteedUnderwritingSystem,
        OtherAUS,
        InternalProprietarySystem,
        EmptyAUSValue
    )

    (lar.AUS.aus1 is containedIn(aus1List)) and (lar.AUS.aus2 is containedIn(ausOtherList)) and
    (lar.AUS.aus3 is containedIn(ausOtherList)) and (lar.AUS.aus4 is containedIn(ausOtherList)) and
    (lar.AUS.aus5 is containedIn(ausOtherList))
  }

}
