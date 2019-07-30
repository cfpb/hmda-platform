package hmda.validation.rules.lar.quality.common

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

import scala.util.Try

object Q616_1 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "Q616-1"

  override def parent: String = "Q616"

  override def apply(lar: LoanApplicationRegister): ValidationResult = {
    val dP =
      Try(lar.loanDisclosure.discountPoints.toDouble).getOrElse(0.0)
    val tlc = Try(lar.loanDisclosure.totalLoanCosts.toDouble).getOrElse(0.0)

    when(
      lar.loanDisclosure.discountPoints not oneOf("NA", "Exempt") and
        (lar.loanDisclosure.totalLoanCosts not oneOf("NA", "Exempt"))
        and (dP not equalTo(0.0)) and (tlc not equalTo(0.0))) {
      tlc is greaterThan(dP)
    }
  }
}
