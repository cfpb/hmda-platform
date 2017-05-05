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

object Q071 {
  def inContext(ctx: ValidationContext): AggregateEditCheck[LoanApplicationRegisterSource, LoanApplicationRegister] = {
    IfContextPresentInAggregate(ctx) { new Q071(_, _) }
  }
}

class Q071 private (institution: Institution, year: Int) extends AggregateEditCheck[LoanApplicationRegisterSource, LoanApplicationRegister] {
  override def name: String = "Q071"

  val configuration = ConfigFactory.load()
  val threshold = configuration.getInt("hmda.validation.macro.Q071.currentYearThreshold")
  val minProportionSold = configuration.getDouble("hmda.validation.macro.Q071.currentYearProportion")
  val yearDifference = configuration.getDouble("hmda.validation.macro.Q071.relativeProportion")

  override def apply[as: AS, mat: MAT, ec: EC](lars: LoanApplicationRegisterSource): Future[Result] = {
    val relevantLars = lars.filter(relevant)
    val numRelevant = count(relevantLars)
    val numRelevantSold = count(relevantLars.filter(relevantSold))

    for {
      r <- numRelevant
      rs <- numRelevantSold
    } yield {
      println(s"2017: Counted $r relevant loans")
      println(s"2017: Counted $rs relevant loans sold to GM")
      println(s"2017: That makes ${rs.toDouble / r} the proportion sold")

      when(r is greaterThanOrEqual(threshold)) {
        (rs.toDouble / r) is greaterThan(minProportionSold)
      }
    }
  }

  private def relevant(lar: LoanApplicationRegister): Boolean = {
    val loan = lar.loan
    (lar.actionTakenType == 1 || lar.actionTakenType == 6) &&
      (loan.purpose == 1 || loan.purpose == 3) &&
      (loan.propertyType == 1 || loan.propertyType == 2) &&
      (loan.loanType == 2)
  }

  private def relevantSold(lar: LoanApplicationRegister): Boolean = {
    relevant(lar) && lar.purchaserType == 2
  }
}
