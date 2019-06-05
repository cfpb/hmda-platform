package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

import scala.util.Try

object V688_1 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "V688-1"

  override def parent: String = "V688"

  override def apply(lar: LoanApplicationRegister): ValidationResult = {
    val propValue = Try(lar.property.propertyValue.toDouble).getOrElse(-1.0)

    lar.property.propertyValue is oneOf("Exempt", "NA") or
      (propValue is greaterThan(0))
  }
}
