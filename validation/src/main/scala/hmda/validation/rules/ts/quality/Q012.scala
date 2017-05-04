package hmda.validation.rules.ts.quality

import akka.pattern.ask
import hmda.validation._
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import hmda.model.fi.ts.TransmittalSheet
import hmda.model.institution.Institution
import hmda.validation.{ AS, EC, MAT }
import hmda.validation.ValidationStats.FindTaxId
import hmda.validation.context.ValidationContext
import hmda.validation.rules._
import hmda.validation.dsl.Result
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._

import scala.concurrent.Future
import scala.concurrent.duration._

object Q012 {
  def inContext(ctx: ValidationContext): AggregateEditCheck[TransmittalSheet, TransmittalSheet] = {
    IfContextPresentInAggregate(ctx) { new Q012(_, _) }
  }
}

class Q012 private (institution: Institution, year: Int) extends AggregateEditCheck[TransmittalSheet, TransmittalSheet] {
  override def name: String = "Q012"

  override def apply[as: AS, mat: MAT, ec: EC](input: TransmittalSheet): Future[Result] = {
    val system = implicitly[AS[_]]
    val configuration = ConfigFactory.load()
    val duration = configuration.getInt("hmda.actor.timeout")
    implicit val timeout = Timeout(duration.seconds)

    val validationStats = system.actorSelection("/user/validation-stats")
    val fLastYearTaxId = (validationStats ? FindTaxId(institution.id, (year - 1).toString)).mapTo[String]

    for {
      l <- fLastYearTaxId
    } yield {
      when(l not empty) {
        l is input.taxId
      }
    }
  }
}
