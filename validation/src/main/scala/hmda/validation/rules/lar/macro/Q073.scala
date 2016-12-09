package hmda.validation.rules.lar.`macro`

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory
import hmda.model.fi.lar.LoanApplicationRegister
import hmda.model.fi.lar.fields.LarTopLevelFields._
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.Result
import hmda.validation.rules.AggregateEditCheck
import hmda.validation.rules.lar.`macro`.MacroEditTypes._

import scala.concurrent.{ ExecutionContext, Future }

object Q073 extends AggregateEditCheck[LoanApplicationRegisterSource, LoanApplicationRegister] {

  val config = ConfigFactory.load()
  val count = config.getInt("hmda.validation.macro.Q073.numOfLoanApplications")
  val multiplier = config.getDouble("hmda.validation.macro.Q073.numOfLarsMultiplier")

  override def name = "Q073"

  override def fields(lars: LoanApplicationRegisterSource) = Map(noField -> "")

  override def apply(lars: LoanApplicationRegisterSource)(implicit system: ActorSystem, materializer: ActorMaterializer, ec: ExecutionContext): Future[Result] = {
    val applicableLoans = lars.filter(lar =>
      lar.loan.purpose == 1
        && Seq(1, 6).contains(lar.actionTakenType)
        && Seq(1, 2).contains(lar.loan.propertyType)
        && Seq(2, 3).contains(lar.loan.loanType))

    val purchaserType = count(applicableLoans)

    val sold = count(applicableLoans.filter(lar => lar.purchaserType != 0))

    for {
      r <- purchaserType
      d <- sold
    } yield {
      when(r is greaterThanOrEqual(count)) {
        d.toDouble is greaterThan(r * multiplier)
      }
    }

  }
}
