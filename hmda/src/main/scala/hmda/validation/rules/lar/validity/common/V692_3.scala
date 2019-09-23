package hmda.validation.rules.lar.validity

import com.typesafe.config.ConfigFactory
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

import scala.util.Try

object V692_3 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "V692-3"

  override def parent: String = "V692"

  override def apply(lar: LoanApplicationRegister): ValidationResult = {
    val config = ConfigFactory.load()
    val units  = config.getInt("edits.V692.units")

    val mau =
      Try(lar.property.multiFamilyAffordableUnits.toInt).getOrElse(Int.MaxValue)

    when(lar.property.totalUnits is greaterThanOrEqual(units)) {
      mau is lessThanOrEqual(lar.property.totalUnits) or
        (lar.property.multiFamilyAffordableUnits is oneOf("NA", "Exempt"))
    }
  }
}
