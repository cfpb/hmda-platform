package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

import scala.util.Try

object V674_1 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "V674-1"

  override def parent: String = "V674"

  override def apply(lar: LoanApplicationRegister): ValidationResult = {
    val oc = Try(lar.loanDisclosure.originationCharges.toDouble).getOrElse(-1.0)
    lar.loanDisclosure.originationCharges is oneOf("Exempt", "NA") or
      (oc is greaterThanOrEqual(0.0))
  }
}
