package hmda.validation.rules.lar.`macro`

import akka.pattern.ask
import hmda.model.fi.lar.LoanApplicationRegister
import hmda.model.institution.Institution
import hmda.validation.ValidationStats.FindQ076
import hmda.validation.context.ValidationContext
import hmda.validation.dsl.Result
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
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

class Q076 private (institution: Institution, year: Int) extends AggregateEditCheck[LoanApplicationRegisterSource, LoanApplicationRegister] with StatsLookup {
  override def name: String = "Q076"

  val threshold = configuration.getInt("hmda.validation.macro.Q076.threshold")
  val yearDifference = configuration.getDouble("hmda.validation.macro.Q076.relativeProportion")

  override def apply[as: AS, mat: MAT, ec: EC](lars: LoanApplicationRegisterSource): Future[Result] = {
    val relevantLars = lars.filter(Q076.relevant)
    val numRelevant = count(relevantLars)
    val numRelevantSold = count(relevantLars.filter(Q076.sold))

    val lastYearStats = for {
      actorRef <- validationStats
      stats <- (actorRef ? FindQ076(institution.id, (year - 1).toString)).mapTo[Double]
    } yield stats

    for {
      r <- numRelevant
      rs <- numRelevantSold
      percentageSoldPreviousYear <- lastYearStats
    } yield {

      val percentageSoldCurrentYear = rs.toDouble / r

      when(r is greaterThan(threshold)) {
        math.abs(percentageSoldCurrentYear - percentageSoldPreviousYear) is lessThan(yearDifference)
      }
    }

  }
}
