package hmda.validation.rules.lar.`macro`

import akka.pattern.ask
import hmda.model.fi.lar.LoanApplicationRegister
import hmda.model.institution.Institution
import hmda.validation.ValidationStats.FindQ072
import hmda.validation.{AS, EC, MAT}
import hmda.validation.context.ValidationContext
import hmda.validation.dsl.Result
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.rules.{AggregateEditCheck, IfContextPresentInAggregate, StatsLookup}
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

class Q072 private (institution: Institution, year: Int) extends AggregateEditCheck[LoanApplicationRegisterSource, LoanApplicationRegister] with StatsLookup {
  override def name: String = "Q072"

  val threshold = configuration.getInt("hmda.validation.macro.Q072.currentYearThreshold")
  val minProportionSold = configuration.getDouble("hmda.validation.macro.Q072.currentYearProportion")
  val yearDifference = configuration.getDouble("hmda.validation.macro.Q072.relativeProportion")

  override def apply[as: AS, mat: MAT, ec: EC](lars: LoanApplicationRegisterSource): Future[Result] = {
    val relevantLars = lars.filter(Q072.relevant)
    val numRelevant = count(relevantLars)
    val numRelevantSold = count(relevantLars.filter(Q072.sold))

    val lastYearLars = (validationStats ? FindQ072(institution.id, (year - 1).toString)).mapTo[(Int, Int)]

    for {
      r <- numRelevant
      rs <- numRelevantSold
      ly <- lastYearLars
    } yield {
      val (lastYearRelevant, lastYearSold) = ly
      val percentageSoldCurrentYear = rs.toDouble / r
      val percentageSoldPreviousYear = lastYearSold.toDouble / lastYearRelevant

      val check1Fails = (percentageSoldPreviousYear - percentageSoldCurrentYear) not lessThan(yearDifference)
      val check2Fails = percentageSoldCurrentYear not greaterThan(minProportionSold)
      val check1NotApplicable = percentageSoldCurrentYear not lessThan(percentageSoldPreviousYear)
      val check2NotApplicable = r not greaterThanOrEqual(threshold)

      when(check1Fails) {
        check1NotApplicable
      } and when(check2Fails) {
        check2NotApplicable
      }

    }
  }
}
