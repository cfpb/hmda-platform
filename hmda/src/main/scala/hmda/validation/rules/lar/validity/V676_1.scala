package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

import scala.util.Try

object V676_1 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "V676-1"

  override def parent: String = "V676"

  override def apply(lar: LoanApplicationRegister): ValidationResult = {
    val lc = Try(lar.loanDisclosure.lenderCredits.toDouble).getOrElse(-1.0)
    lar.loanDisclosure.lenderCredits is oneOf("Exempt", "NA", "") or
      (lc is greaterThan(0.0))
  }
}
