package hmda.validation.rules.lar.`macro`

import akka.actor.ActorSystem
import akka.pattern.ask
import akka.stream.ActorMaterializer
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import hmda.model.fi.SubmissionId
import hmda.model.fi.lar.LoanApplicationRegister
import hmda.model.institution.Institution
import hmda.persistence.messages.events.processing.CommonHmdaValidatorEvents.LarValidated
import hmda.validation.context.ValidationContext
import hmda.validation.dsl.Result
import hmda.validation.rules.AggregateEditCheck
import hmda.validation.rules.lar.`macro`.MacroEditTypes.LoanApplicationRegisterSource

import scala.concurrent.{ ExecutionContext, Future }
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.rules.IfInstitutionPresentInAggregate
import hmda.persistence.processing.HmdaQuery._
import hmda.validation.ValidationStats.FindTotalLars
import scala.concurrent.duration._

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

    val configuration = ConfigFactory.load()
    val duration = configuration.getInt("hmda.actor.timeout")
    implicit val timeout = Timeout(duration.seconds)

    val lastYear = year - 1
    val currentLarCount: Future[Int] = count(lars)

    val validationStats = system.actorSelection("/user/validation-stats")

    val lastYearCount = (validationStats ? FindTotalLars(institution.id, lastYear.toString)).mapTo[Int]

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
