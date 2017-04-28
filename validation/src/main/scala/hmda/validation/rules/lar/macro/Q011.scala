package hmda.validation.rules.lar.`macro`

import hmda.validation._
import akka.pattern.ask
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import hmda.model.fi.lar.LoanApplicationRegister
import hmda.model.institution.Institution
import hmda.validation.context.ValidationContext
import hmda.validation.dsl.Result
import hmda.validation.rules._
import hmda.validation.rules.lar.`macro`.MacroEditTypes.LoanApplicationRegisterSource
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.ValidationStats.FindTotalLars

import scala.concurrent.duration._
import scala.concurrent.Future

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

  override def apply[as: AS, mat: MAT, ec: EC](lars: LoanApplicationRegisterSource): Future[Result] = {

    val system = implicitly[AS[_]]

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
      lowerValue = l * (1 - multiplier)
      upperValue = l * (1 + multiplier)
    } yield {
      when(c is greaterThanOrEqual(larSize) or (l is greaterThanOrEqual(larSize))) {
        c.toDouble is between(lowerValue, upperValue)
      }
    }
  }

}
