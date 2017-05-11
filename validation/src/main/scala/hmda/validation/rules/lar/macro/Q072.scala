package hmda.validation.rules.lar.`macro`

import com.typesafe.config.ConfigFactory
import hmda.model.fi.lar.LoanApplicationRegister
import hmda.model.institution.Institution
import hmda.validation.{ AS, EC, MAT }
import hmda.validation.context.ValidationContext
import hmda.validation.dsl.Result
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.rules.{ AggregateEditCheck, IfContextPresentInAggregate }
import hmda.validation.rules.lar.`macro`.MacroEditTypes.LoanApplicationRegisterSource

import scala.concurrent.Future

object Q072 {
  def inContext(ctx: ValidationContext): AggregateEditCheck[LoanApplicationRegisterSource, LoanApplicationRegister] = {
    IfContextPresentInAggregate(ctx) { new Q072(_, _) }
  }

  def relevant(lar: LoanApplicationRegister): Boolean = {
    val loan = lar.loan
    (lar.actionTakenType == 1 || lar.actionTakenType == 6) &&
      (loan.purpose == 1 || loan.purpose == 3) &&
      (loan.propertyType == 1 || loan.propertyType == 2) &&
      (loan.loanType == 3)
  }

  def sold(lar: LoanApplicationRegister): Boolean = {
    lar.purchaserType == 2
  }
}

class Q072 private (institution: Institution, year: Int) extends AggregateEditCheck[LoanApplicationRegisterSource, LoanApplicationRegister] {
  override def name: String = "Q072"

  val configuration = ConfigFactory.load()
  val threshold = configuration.getInt("hmda.validation.macro.Q072.currentYearThreshold")
  val minProportionSold = configuration.getDouble("hmda.validation.macro.Q072.currentYearProportion")
  val yearDifference = configuration.getDouble("hmda.validation.macro.Q072.relativeProportion")

  override def apply[as: AS, mat: MAT, ec: EC](lars: LoanApplicationRegisterSource): Future[Result] = {
    val relevantLars = lars.filter(Q072.relevant)
    val numRelevant = count(relevantLars)
    val numRelevantSold = count(relevantLars.filter(Q072.sold))

    for {
      r <- numRelevant
      rs <- numRelevantSold
    } yield {
      r is greaterThan(1)

      /* // current year check
      when(r is greaterThanOrEqual(threshold)) {
        (rs.toDouble / r) is greaterThan(minProportionSold)
      }
      */
    }
  }
}
