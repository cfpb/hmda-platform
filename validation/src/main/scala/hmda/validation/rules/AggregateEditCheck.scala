package hmda.validation.rules

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{ Sink, Source }
import hmda.model.fi.RecordField
import hmda.validation.dsl.Result

import scala.concurrent.{ ExecutionContext, Future }

abstract class AggregateEditCheck[A <: Source[T, NotUsed], T] extends SourceUtils {

  def name: String

  def fields(input: A): Map[RecordField, String]

  def apply(input: A)(implicit system: ActorSystem, materializer: ActorMaterializer, ec: ExecutionContext): Future[Result]

}
