package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

import scala.util.Try

object V683 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "V683"

  override def apply(lar: LoanApplicationRegister): ValidationResult = {
    val irpNum = Try(lar.loan.introductoryRatePeriod.toInt).getOrElse(-1)
    lar.loan.introductoryRatePeriod is oneOf("Exempt", "NA") or
      (irpNum is greaterThan(0))
  }
}
