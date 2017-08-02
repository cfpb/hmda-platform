package hmda.validation.rules.ts.quality

import akka.pattern.ask
import hmda.model.fi.ts.TransmittalSheet
import hmda.model.institution.Institution
import hmda.validation.ValidationStats.FindTotalSubmittedLars
import hmda.validation._
import hmda.validation.context.ValidationContext
import hmda.validation.rules.{ AggregateEditCheck, IfContextPresentInAggregate, StatsLookup }
import hmda.validation.dsl.Result
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._

import scala.concurrent.Future

object Q130 {
  def inContext(ctx: ValidationContext): AggregateEditCheck[TransmittalSheet, TransmittalSheet] = {
    IfContextPresentInAggregate(ctx) { new Q130(_, _) }
  }
}

class Q130 private (institution: Institution, year: Int) extends AggregateEditCheck[TransmittalSheet, TransmittalSheet] with StatsLookup {
  override def name: String = "Q130"

  override def apply[as: AS, mat: MAT, ec: EC](input: TransmittalSheet): Future[Result] = {

    val fSubmittedLars = (validationStats ? FindTotalSubmittedLars(institution.id, year.toString)).mapTo[Int]

    for {
      submitted <- fSubmittedLars
    } yield {
      submitted is input.totalLines
    }
  }
}
