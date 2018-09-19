package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

import scala.util.Try

object V673_1 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "V673-1"

  override def parent: String = "V673"

  override def apply(lar: LoanApplicationRegister): ValidationResult = {
    val tpf =
      Try(lar.loanDisclosure.totalPointsAndFees.toDouble).getOrElse(-1.0)
    lar.loanDisclosure.totalPointsAndFees is oneOf("Exempt", "NA") or
      (tpf is greaterThanOrEqual(0.0))
  }
}
