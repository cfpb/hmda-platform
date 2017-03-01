package hmda.validation.rules

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import hmda.validation.dsl.Result
import scala.language.higherKinds
import scala.concurrent.{ ExecutionContext, Future }

abstract class AggregateEditCheck[-S, +T] extends SourceUtils {

  def name: String

  def apply(input: S)(implicit system: ActorSystem, materializer: ActorMaterializer, ec: ExecutionContext): Future[Result]

}
