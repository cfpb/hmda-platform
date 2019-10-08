package hmda.validation.rules.lar.quality.common

import com.typesafe.config.ConfigFactory
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums.HOEPStatusANotApplicable
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

object Q630 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "Q630"

  override def apply(lar: LoanApplicationRegister): ValidationResult = {
    val config = ConfigFactory.load()
    val units  = config.getInt("edits.Q630.units")

    when(lar.property.totalUnits is greaterThanOrEqual(units)) {
      lar.hoepaStatus is equalTo(HOEPStatusANotApplicable)
    }
  }
}
