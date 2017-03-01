package hmda.validation.rules.lar.`macro`

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory
import hmda.model.fi.lar.LoanApplicationRegister
import hmda.model.institution.Institution
import hmda.query.DbConfiguration
import hmda.query.repository.filing.FilingComponent
import hmda.validation.context.ValidationContext
import hmda.validation.dsl.Result
import hmda.validation.rules.{ AggregateEditCheck, IfYearPresentInAggregate }
import hmda.validation.rules.lar.`macro`.MacroEditTypes.LoanApplicationRegisterSource

import scala.concurrent.{ ExecutionContext, Future }
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._

object Q011 {
  def inContext(ctx: ValidationContext): AggregateEditCheck[LoanApplicationRegisterSource, LoanApplicationRegister] = {
    IfYearPresentInAggregate(ctx) { new Q011(_, _) }
  }
}

class Q011 private (institution: Institution, year: Int) extends AggregateEditCheck[LoanApplicationRegisterSource, LoanApplicationRegister] with FilingComponent with DbConfiguration {
  override def name: String = "Q011"

  val configuration = ConfigFactory.load()
  val previousFixed = configuration.getInt("hmda.validation.macro.Q011.lar.size")
  val multiplier = configuration.getInt("hmda.validation.macro.Q011.lar.multiplier")

  val larTotalRepository = new LarTotalRepository(config)

  override def apply(lars: LoanApplicationRegisterSource)(implicit system: ActorSystem, materializer: ActorMaterializer, ec: ExecutionContext): Future[Result] = {
    val lastYear = year - 1
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
