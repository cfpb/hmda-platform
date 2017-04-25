package hmda.validation.rules

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import hmda.validation.dsl.Result

import scala.concurrent.{ ExecutionContext, Future }

abstract class AggregateEditCheck[-A, +B] extends SourceUtils {

  def name: String

  def apply(input: A)(implicit system: ActorSystem, materializer: ActorMaterializer, ec: ExecutionContext): Future[Result]

}
