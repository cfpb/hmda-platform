package hmda.validation.rules.ts.quality

import akka.pattern.ask
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import hmda.model.fi.ts.TransmittalSheet
import hmda.model.institution.Institution
import hmda.validation.ValidationStats.FindTotalSubmittedLars
import hmda.validation._
import hmda.validation.context.ValidationContext
import hmda.validation.rules.{ AggregateEditCheck, IfContextPresentInAggregate }
import hmda.validation.dsl.Result
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._

import scala.concurrent.Future
import scala.concurrent.duration._

object Q130 {
  def inContext(ctx: ValidationContext): AggregateEditCheck[TransmittalSheet, TransmittalSheet] = {
    IfContextPresentInAggregate(ctx) { new Q130(_, _) }
  }
}

class Q130 private (institution: Institution, year: Int) extends AggregateEditCheck[TransmittalSheet, TransmittalSheet] {
  override def name: String = "Q130"

  override def apply[as: AS, mat: MAT, ec: EC](input: TransmittalSheet): Future[Result] = {
    val system = implicitly[AS[_]]
    val configuration = ConfigFactory.load()
    val duration = configuration.getInt("hmda.actor.timeout")
    implicit val timeout = Timeout(duration.seconds)

    val validationStats = system.actorSelection("/user/validation-stats")
    val fSubmittedLars = (validationStats ? FindTotalSubmittedLars(institution.id, year.toString)).mapTo[Int]

    for {
      submitted <- fSubmittedLars
    } yield {
      submitted is input.totalLines
    }
  }
}
