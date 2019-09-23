package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums.ReverseMortgage
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

object V674_2 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "V674-2"

  override def parent: String = "V674"

  override def apply(lar: LoanApplicationRegister): ValidationResult =
    when(lar.reverseMortgage is equalTo(ReverseMortgage)) {
      lar.loanDisclosure.originationCharges is oneOf("NA", "Exempt")
    }
}
