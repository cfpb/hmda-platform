package hmda.validation.rules.lar.quality._2025

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck
import hmda.validation.dsl.PredicateHmda._
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._


object Q646 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "Q646"

  override def parent: String = "Q646"
  override def apply(lar: LoanApplicationRegister): ValidationResult = {
    exemptionTaken(lar) is equalTo(false)
  }
}