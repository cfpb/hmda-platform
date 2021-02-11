package hmda.validation.rules.lar.quality._2021

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax.PredicateOps
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

import scala.util.Try

object Q655 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "Q655"

  override def apply(lar: LoanApplicationRegister): ValidationResult ={
    when(lar.property.totalUnits is greaterThanOrEqual(5)) {
      lar.property.multiFamilyAffordableUnits not equalTo("NA") and
        (lar.property.multiFamilyAffordableUnits is equalTo("Exempt") or
      (Try(lar.property.multiFamilyAffordableUnits.toInt).isSuccess is equalTo(true)))
    }
  }
}