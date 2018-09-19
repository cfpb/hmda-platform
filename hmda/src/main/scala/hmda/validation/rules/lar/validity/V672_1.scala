package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

import scala.util.Try

object V672_1 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "V672-1"

  override def parent: String = "V672"

  override def apply(lar: LoanApplicationRegister): ValidationResult = {
    val tlc = Try(lar.loanDisclosure.totalLoanCosts.toDouble).getOrElse(-1.0)
    lar.loanDisclosure.totalLoanCosts is oneOf("Exempt", "NA") or
      (tlc is greaterThanOrEqual(0.0))
  }
}
