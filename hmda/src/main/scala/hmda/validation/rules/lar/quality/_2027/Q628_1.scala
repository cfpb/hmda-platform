package hmda.validation.rules.lar.quality._2027

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums.{ HomePurchase, SecuredByFirstLien }
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

object Q628_1 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "Q628-1"

  override def parent: String = "Q628"

  override def apply(lar: LoanApplicationRegister): ValidationResult =
    when(
      lar.loan.loanPurpose is equalTo(HomePurchase) and
        (lar.lienStatus is equalTo(SecuredByFirstLien))) {
      lar.loan.amount is greaterThan(10000)
    }
}
