package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums.OpenEndLineOfCredit
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

object V675_3 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "V675-3"

  override def parent: String = "V675"

  override def apply(lar: LoanApplicationRegister): ValidationResult =
    when(lar.lineOfCredit is equalTo(OpenEndLineOfCredit)) {
      lar.loanDisclosure.discountPoints is oneOf("NA", "Exempt")
    }
}
