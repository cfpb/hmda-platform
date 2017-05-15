package hmda.validation.rules.lar.`macro`

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.model.institution.Institution
import hmda.validation.context.ValidationContext
import hmda.validation.dsl.Result
import hmda.validation.rules.lar.`macro`.MacroEditTypes.LoanApplicationRegisterSource
import hmda.validation.{ AS, EC, MAT }
import hmda.validation.rules.{ AggregateEditCheck, IfContextPresentInAggregate, StatsLookup }

import scala.concurrent.Future

object Q075 {
  def inContext(ctx: ValidationContext): AggregateEditCheck[LoanApplicationRegisterSource, LoanApplicationRegister] = {
    IfContextPresentInAggregate(ctx) { new Q075(_, _) }
  }

}

class Q075 private (institution: Institution, year: Int) extends AggregateEditCheck with StatsLookup {
  override def name: String = "Q075"

  override def apply[as: AS, mat: MAT, ec: EC](input: Any): Future[Result] = ???
}
