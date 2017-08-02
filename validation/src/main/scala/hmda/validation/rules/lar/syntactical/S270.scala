package hmda.validation.rules.lar.syntactical

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.context.ValidationContext
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax.PredicateOps
import hmda.validation.dsl.Result
import hmda.validation.rules.{ EditCheck, IfYearPresentIn }

object S270 {
  def inContext(ctx: ValidationContext): EditCheck[LoanApplicationRegister] = {
    IfYearPresentIn(ctx) { new S270(_) }
  }
}

class S270 private (year: Int) extends EditCheck[LoanApplicationRegister] {
  override def name: String = "S270"

  override def apply(lar: LoanApplicationRegister): Result = {
    val larYear = lar.actionTakenDate.toString.substring(0, 4).toInt
    larYear is equalTo(year)
  }
}
