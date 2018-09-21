package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

import scala.util.Try

object V692_1 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "V692-1"

  override def parent: String = "V692"

  override def apply(lar: LoanApplicationRegister): ValidationResult = {
    val mauIsNum = Try(lar.property.multiFamilyAffordableUnits.toInt).isSuccess

    lar.property.multiFamilyAffordableUnits is oneOf("NA", "Exempt") or
      (mauIsNum is equalTo(true))
  }
}
