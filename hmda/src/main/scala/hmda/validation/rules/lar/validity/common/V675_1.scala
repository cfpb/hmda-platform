package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

import scala.util.Try

object V675_1 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "V675-1"

  override def parent: String = "V675"

  override def apply(lar: LoanApplicationRegister): ValidationResult = {
    val discount =
      Try(lar.loanDisclosure.discountPoints.toDouble).getOrElse(-1.0)
    lar.loanDisclosure.discountPoints is oneOf("Exempt", "NA", "") or
      (discount is greaterThan(0.0))
  }
}
