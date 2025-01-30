package hmda.validation.rules.lar.syntactical

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.ts.TransmittalSheet
import hmda.validation.context.ValidationContext
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.{ EditCheck, IfTsPresentIn }

object S301 {
  def withContext(ctx: ValidationContext): EditCheck[LoanApplicationRegister] = {
    IfTsPresentIn(ctx) { new S301(_) }
  }

}

class S301 private (ts: TransmittalSheet) extends EditCheck[LoanApplicationRegister] {
  override def name: String = "S301"

  override def apply(lar: LoanApplicationRegister): ValidationResult =
    lar.larIdentifier.LEI.toLowerCase is equalTo(ts.LEI.toLowerCase)
}
