package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

import scala.util.Try

object V682_1 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "V682-1"

  override def parent: String = "V682"

  override def apply(lar: LoanApplicationRegister): ValidationResult = {
    val lt = Try(lar.loan.loanTerm.toInt).getOrElse(-1)
    lar.loan.loanTerm is oneOf("NA", "Exempt") or (lt is greaterThan(0))
  }
}
