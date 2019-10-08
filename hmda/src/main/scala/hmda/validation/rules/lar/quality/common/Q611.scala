package hmda.validation.rules.lar.quality.common

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums.{ HighCostMortgage, LoanOriginated, SecuredBySubordinateLien }
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

import scala.util.Try

object Q611 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "Q611"

  override def apply(lar: LoanApplicationRegister): ValidationResult = {
    val rs = Try(lar.loan.rateSpread.toDouble).getOrElse(0.0)
    when(
      lar.action.actionTakenType is equalTo(LoanOriginated) and (lar.lienStatus is equalTo(SecuredBySubordinateLien)) and (rs is greaterThan(
        8.5
      ))
    ) {
      lar.hoepaStatus is equalTo(HighCostMortgage)
    }
  }
}
