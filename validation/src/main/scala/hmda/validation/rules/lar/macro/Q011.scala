package hmda.validation.rules.lar.`macro`

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory
import hmda.model.fi.lar.LoanApplicationRegister
import hmda.model.institution.Institution
import hmda.validation.context.ValidationContext
import hmda.validation.dsl.Result
import hmda.validation.rules.AggregateEditCheck
import hmda.validation.rules.lar.`macro`.MacroEditTypes.LoanApplicationRegisterSource

import scala.concurrent.{ ExecutionContext, Future }
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.rules.IfInstitutionPresentInAggregate

object Q011 {
  def inContext(ctx: ValidationContext): AggregateEditCheck[LoanApplicationRegisterSource, LoanApplicationRegister] = {
    IfInstitutionPresentInAggregate(ctx) { new Q011(_, _) }
  }
}

class Q011 private (institution: Institution, year: Int) extends AggregateEditCheck[LoanApplicationRegisterSource, LoanApplicationRegister] {
  override def name: String = "Q011"

  val configuration = ConfigFactory.load()
  val larSize = configuration.getInt("hmda.validation.macro.Q011.numOfTotalLars")
  val multiplier = configuration.getDouble("hmda.validation.macro.Q011.numOfLarsMultiplier")

  override def apply(lars: LoanApplicationRegisterSource)(implicit system: ActorSystem, materializer: ActorMaterializer, ec: ExecutionContext): Future[Result] = {
    val lastYear = year - 1
    val currentLarCount: Future[Int] = count(lars)
    val lastYearCount: Future[Int] = Future(500) //TODO: implement counting on last years source

    for {
      c <- currentLarCount
      l <- lastYearCount
      lower = 1 * (1 - multiplier)
      upper = 1 * (1 + multiplier)
    } yield {
      when(c is greaterThanOrEqual(larSize) or (l is greaterThanOrEqual(larSize))) {
        c.toDouble.toString is numericallyBetween(lower.toString, upper.toString)
      }
    }
  }
}
