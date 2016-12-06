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
import hmda.validation.rules.lar.`macro`.MacroEditTypes.LoanApplicationRegisterSource

import scala.concurrent.{ ExecutionContext, Future }

object Q062 extends AggregateEditCheck[LoanApplicationRegisterSource, LoanApplicationRegister] {

  val config = ConfigFactory.load()
  val multiplier = config.getDouble("hmda.validation.macro.Q062.numOfLarsMultiplier")

  override def name: String = "Q062"

  override def description = ""

  override def fields(lars: LoanApplicationRegisterSource) = Map(noField -> "")

  override def apply(lars: LoanApplicationRegisterSource)(implicit system: ActorSystem, materializer: ActorMaterializer, ec: ExecutionContext): Future[Result] = {

    val fannieHoepaLoans =
      count(lars.filter(
        lar => {
          lar.actionTakenType == 1 &&
            lar.hoepaStatus == 1 &&
            lar.lienStatus == 1 &&
            lar.purchaserType == 1
        }
      ))

    val originated = count(lars.filter(lar => lar.actionTakenType == 1))

    for {
      f <- fannieHoepaLoans
      o <- originated
    } yield {
      f.toDouble is lessThanOrEqual(o * multiplier)
    }
  }
}
