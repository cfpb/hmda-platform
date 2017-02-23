package hmda.validation.rules

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.context.ValidationContext
import hmda.validation.rules.lar.`macro`.MacroEditTypes.LoanApplicationRegisterSource

object IfYearPresentInAggregate {
  def apply(ctx: ValidationContext)(constructor: (Int) => AggregateEditCheck[LoanApplicationRegisterSource, LoanApplicationRegister]) = {
    ctx.filingYear match {
      case Some(year) => constructor(year)
      case None => new EmptyAggregateEditCheck
    }
  }
}
