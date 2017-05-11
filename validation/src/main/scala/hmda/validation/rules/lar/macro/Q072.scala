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
    val currentYearCount = count(relevantLars)
    val currentYearSold = count(relevantLars.filter(Q072.sold))

    val previousYearCount = 0
    val previousYearSold = 0

    for {
      relevantCount <- currentYearCount
      relevantSold <- currentYearSold
    } yield {
      val currentRatio = relevantSold.toDouble / relevantCount
      val previousRatio = previousYearSold.toDouble / previousYearCount

      when(currentRatio is lessThan(previousRatio)) {
        (previousRatio - currentRatio) is greaterThanOrEqual(yearDifference)
      } or when(relevantCount is greaterThanOrEqual(threshold)) {
        currentRatio is greaterThanOrEqual(minProportionSold)
      }
    }
  }
}
