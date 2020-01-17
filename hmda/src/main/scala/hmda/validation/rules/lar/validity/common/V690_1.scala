package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums.InvalidManufacturedHomeLandPropertyCode
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

object V690_1 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "V690-1"

  override def parent: String = "V690"

  override def apply(lar: LoanApplicationRegister): ValidationResult =
    lar.property.manufacturedHomeLandPropertyInterest not equalTo(new InvalidManufacturedHomeLandPropertyCode)
}
