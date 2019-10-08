package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

import scala.util.Try

object V678_5 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "V678-5"

  override def parent: String = "V678"

  override def apply(lar: LoanApplicationRegister): ValidationResult =
    when(lar.loan.prepaymentPenaltyTerm is numeric and (lar.loan.loanTerm is numeric)) {
      val ppt =
        Try(lar.loan.prepaymentPenaltyTerm.toInt).getOrElse(Int.MaxValue)
      val lt = Try(lar.loan.loanTerm.toInt).getOrElse(Int.MinValue)
      ppt is lessThanOrEqual(lt)
    }
}
