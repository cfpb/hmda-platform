package hmda.validation.rules.ts.quality

import akka.pattern.ask
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import hmda.model.fi.ts.TransmittalSheet
import hmda.model.institution.Institution
import hmda.validation.ValidationStats.FindTaxId
import hmda.validation.context.ValidationContext
import hmda.validation.rules.{AggregateEditCheck, IfInstitutionPresentIn}
import hmda.validation.dsl.Result
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._


object Q012 {
  def inContext(ctx: ValidationContext): AggregateEditCheck[TransmittalSheet, TransmittalSheet] = {
    IfInstitutionPresentIn(ctx) { new Q012(_) }
  }
}

class Q012 private (institution: Institution) extends AggregateEditCheck[TransmittalSheet, TransmittalSheet] {
  override def name: String = "Q012"

  override def apply(input: TransmittalSheet)(implicit system: ActorSystem, materializer: ActorMaterializer, ec: ExecutionContext): Future[Result] = {
    val configuration = ConfigFactory.load()
    val duration = configuration.getInt("hmda.actor.timeout")
    implicit val timeout = Timeout(duration.seconds)

    val validationStats = system.actorSelection("/user/validation-stats")
    val fLastYearTaxId = (validationStats ? FindTaxId(institution.id, (institution.activityYear - 1).toString)).mapTo[String]

    for {
      l <- fLastYearTaxId
    } yield {
      when(l not empty) {
        l is input.taxId
      }
    }
  }
}
