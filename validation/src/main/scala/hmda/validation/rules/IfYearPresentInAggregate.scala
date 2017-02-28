package hmda.validation.rules

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.model.institution.Institution
import hmda.validation.context.ValidationContext
import hmda.validation.rules.lar.`macro`.MacroEditTypes.LoanApplicationRegisterSource

object IfYearPresentInAggregate {
  def apply(ctx: ValidationContext)(constructor: (Institution, Int) => AggregateEditCheck[LoanApplicationRegisterSource, LoanApplicationRegister]) = {
    ctx match {
      case ValidationContext(Some(institution), Some(year)) => constructor(institution, year)
      case _ => new EmptyAggregateEditCheck
    }
  }
}
