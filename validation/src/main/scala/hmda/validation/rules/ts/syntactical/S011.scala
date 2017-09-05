package hmda.validation.rules.ts.syntactical

import akka.pattern.ask
import hmda.model.fi.ts.TransmittalSheet
import hmda.model.institution.Institution
import hmda.validation.ValidationStats.FindTotalSubmittedLars
import hmda.validation._
import hmda.validation.context.ValidationContext
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.Result
import hmda.validation.rules.{ AggregateEditCheck, IfContextPresentInAggregate, StatsLookup }

import scala.concurrent.Future

object S011 {
  def inContext(ctx: ValidationContext): AggregateEditCheck[TransmittalSheet, TransmittalSheet] = {
    IfContextPresentInAggregate(ctx) { new S011(_, _) }
  }
}

class S011 private (institution: Institution, year: Int) extends AggregateEditCheck[TransmittalSheet, TransmittalSheet] with StatsLookup {
  override def name: String = "S011"

  override def apply[as: AS, mat: MAT, ec: EC](input: TransmittalSheet): Future[Result] = {

    val fSubmittedLars = for {
      actorRef <- validationStats
      totalLars <- (actorRef ? FindTotalSubmittedLars(institution.id, year.toString)).mapTo[Int]
    } yield totalLars

    for {
      submitted <- fSubmittedLars
    } yield {
      submitted is greaterThan(0)
    }
  }
}
