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

object Q081 extends AggregateEditCheck[LoanApplicationRegisterSource, LoanApplicationRegister] {

  val config = ConfigFactory.load()
  val multiplier = config.getDouble("hmda.validation.macro.Q081.numOfLarsMultiplier")

  override def name = "Q081"

  override def apply(lars: LoanApplicationRegisterSource)(implicit system: ActorSystem, materializer: ActorMaterializer, ec: ExecutionContext): Future[Result] = {

    val relevantLars =
      count(lars.filter(lar => (1 to 3).contains(lar.actionTakenType)
        && (1 to 2).contains(lar.loan.propertyType)
        && (6 to 7).contains(lar.applicant.race1)))

    val approvedOrDenied = count(lars.filter(lar => (1 to 5).contains(lar.actionTakenType)))

    for {
      a <- relevantLars
      t <- approvedOrDenied
    } yield {
      a.toDouble is lessThanOrEqual(t * multiplier)
    }
  }
}
