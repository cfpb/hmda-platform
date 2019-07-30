package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

import scala.util.Try

object V672_2 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "V672-2"

  override def parent: String = "V672"

  override def apply(lar: LoanApplicationRegister): ValidationResult = {
    val tpf =
      Try(lar.loanDisclosure.totalPointsAndFees.toDouble).getOrElse(-1.0)
    when(tpf is greaterThanOrEqual(0.0)) {
      lar.loanDisclosure.totalLoanCosts is equalTo("NA")
    }
  }
}
