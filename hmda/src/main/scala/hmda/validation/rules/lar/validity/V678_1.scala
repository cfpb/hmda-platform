package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

import scala.util.Try

object V678_1 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "V678-1"

  override def parent: String = "V678"

  override def apply(lar: LoanApplicationRegister): ValidationResult = {
    val ppt = Try(lar.loan.prepaymentPenaltyTerm.toInt).getOrElse(-1)
    lar.loan.prepaymentPenaltyTerm is oneOf("Exempt", "NA") or
      (ppt is greaterThanOrEqual(0))
  }
}
