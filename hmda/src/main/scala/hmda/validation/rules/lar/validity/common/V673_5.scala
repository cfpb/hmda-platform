package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

import scala.util.Try

object V673_5 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "V673-5"

  override def parent: String = "V673"

  override def apply(lar: LoanApplicationRegister): ValidationResult = {
    val tlc =
      Try(lar.loanDisclosure.totalLoanCosts.toDouble).getOrElse(-1.0)
    when(tlc is greaterThanOrEqual(0.0)) {
      lar.loanDisclosure.totalPointsAndFees is equalTo("NA")
    }
  }
}
