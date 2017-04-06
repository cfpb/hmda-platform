package hmda.validation.rules
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import hmda.validation.dsl.{ Result, Success }

import scala.concurrent.{ ExecutionContext, Future }

class EmptyAggregateEditCheck[A, B] extends AggregateEditCheck[A, B] {
  override def name: String = "empty"

  override def apply(input: A)(implicit system: ActorSystem, materializer: ActorMaterializer, ec: ExecutionContext): Future[Result] = Future(Success())
}
