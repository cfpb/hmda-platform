package hmda.validation.rules.lar.`macro`

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.model.institution.Institution
import hmda.validation.context.ValidationContext
import hmda.validation.dsl.Result
import hmda.validation.rules.lar.`macro`.MacroEditTypes.LoanApplicationRegisterSource
import hmda.validation.{ AS, EC, MAT }
import hmda.validation.rules.{ AggregateEditCheck, IfContextPresentInAggregate, StatsLookup }

import scala.concurrent.Future

object Q076 {
  def inContext(ctx: ValidationContext): AggregateEditCheck[LoanApplicationRegisterSource, LoanApplicationRegister] = {
    IfContextPresentInAggregate(ctx) { new Q076(_, _) }
  }

  def relevant(lar: LoanApplicationRegister): Boolean = {
    (lar.actionTakenType == 1 || lar.actionTakenType == 6) &&
      (lar.loan.propertyType == 1 || lar.loan.propertyType == 2) &&
      lar.loan.purpose == 3
  }

  def sold(lar: LoanApplicationRegister): Boolean = {
    List(1, 2, 3, 4, 5, 6, 7, 8, 9).contains(lar.purchaserType)
  }
}

class Q076 private (institution: Institution, year: Int) extends AggregateEditCheck with StatsLookup {
  override def name: String = "Q076"

  override def apply[as: AS, mat: MAT, ec: EC](input: Any): Future[Result] = ???
}
