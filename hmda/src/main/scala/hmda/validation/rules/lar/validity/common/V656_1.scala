package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._

object V656_1 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "V656-1"

  override def parent: String = "V656"

  override def apply(lar: LoanApplicationRegister): ValidationResult =
    lar.purchaserType not equalTo(new InvalidPurchaserCode)
}
