package hmda.validation.rules.lar.validity

import com.typesafe.config.ConfigFactory
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

object V692_2 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "V692-2"

  override def parent: String = "V692"

  override def apply(lar: LoanApplicationRegister): ValidationResult = {
    val config = ConfigFactory.load()
    val units  = config.getInt("edits.V692.units")

    when(lar.property.totalUnits is lessThan(units)) {
      lar.property.multiFamilyAffordableUnits is oneOf("NA", "Exempt")
    }
  }
}
