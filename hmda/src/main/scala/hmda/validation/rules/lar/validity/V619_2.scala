package hmda.validation.rules.lar.validity

import com.typesafe.config.ConfigFactory
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

object V619_2 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "V619-2"

  override def parent: String = "V619"

  override def apply(lar: LoanApplicationRegister): ValidationResult = {
    val config = ConfigFactory.load()
    val year = config.getInt("edits.V619.year").toString

    lar.action.actionTakenDate.toString.slice(0, 4) is equalTo(year)
  }

}
