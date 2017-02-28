package hmda.validation.rules.lar.`macro`

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory
import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.context.ValidationContext
import hmda.validation.dsl.Result
import hmda.validation.rules.{ AggregateEditCheck, IfYearPresentInAggregate }
import hmda.validation.rules.lar.`macro`.MacroEditTypes.LoanApplicationRegisterSource

import scala.concurrent.{ ExecutionContext, Future }
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._

object Q011 {
  def inContext(ctx: ValidationContext) = {
    IfYearPresentInAggregate(ctx) { new Q011(_) }
  }
}

class Q011 private (year: Int) extends AggregateEditCheck[LoanApplicationRegisterSource, LoanApplicationRegister] {
  override def name: String = "Q011"

  val config = ConfigFactory.load()
  val previousFixed = config.getInt("hmda.validation.macro.Q011.loan.previous.fixed")
  val multiplier = config.getInt("hmda.validation.macro.Q011.loan.actual.multiplier")

  override def apply(lars: LoanApplicationRegisterSource)(implicit system: ActorSystem, materializer: ActorMaterializer, ec: ExecutionContext): Future[Result] = {
    val currentLarCount = count(lars)
    val lastYearLarCount = Future(100) //TODO: implement this!

    for {
      t <- currentLarCount
      l <- lastYearLarCount
      lower = l - l * multiplier
      upper = l + l * multiplier
    } yield {
      when(t is greaterThan(previousFixed) or (l is greaterThan(previousFixed))) {
        t is between(lower, upper)
      }
    }
  }
}
