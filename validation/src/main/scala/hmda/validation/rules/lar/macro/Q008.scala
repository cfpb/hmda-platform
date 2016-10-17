package hmda.validation.rules.lar.`macro`

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.Result
import hmda.validation.rules.AggregateEditCheck
import hmda.validation.rules.lar.`macro`.MacroEditTypes.LoanApplicationRegisterSource

import scala.concurrent.{ ExecutionContext, Future }

object Q008 extends AggregateEditCheck[LoanApplicationRegisterSource, LoanApplicationRegister] {

  override def name = "Q008"

  override def apply(lars: LoanApplicationRegisterSource)(implicit system: ActorSystem, mat: ActorMaterializer, ec: ExecutionContext): Future[Result] = {

    val applicationWithdrawn =
      count(lars.filter(lar => lar.actionTakenType == 4))

    val total = count(lars)

    //TODO: make multiplier configurable
    val multiplier = 0.30

    for {
      a <- applicationWithdrawn
      t <- total
    } yield {
      a is lessThanOrEqual(t * multiplier)
    }

  }
}
