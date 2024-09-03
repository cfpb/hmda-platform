package hmda.validation.rules.lar.quality._2025

import hmda.model.filing.ts.TransmittalSheet
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.context.ValidationContext
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.{ EditCheck, IfTsPresentIn }
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateHmda._
import hmda.validation.dsl.PredicateSyntax._

object Q647 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "Q647"

  override def parent: String = "Q647"
  override def apply(lar: LoanApplicationRegister): ValidationResult = {
    exemptionTaken(lar) is equalTo(false)
  }
}
