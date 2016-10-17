package hmda.validation.rules

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{ Sink, Source }
import hmda.validation.dsl.Result

import scala.concurrent.{ ExecutionContext, Future }

abstract class AggregateEditCheck[A <: Source[T, NotUsed], T] {

  def count(input: Source[T, NotUsed])(implicit materializer: ActorMaterializer): Future[Double] = {
    input.runWith(sinkCount)
  }

  private def sinkCount(): Sink[T, Future[Double]] = {
    Sink.fold[Double, T](0) { (acc, _) =>
      val total = acc + 1
      total.toDouble
    }
  }

  def name: String

  def apply(input: A)(implicit system: ActorSystem, materializer: ActorMaterializer, ec: ExecutionContext): Future[Result]

}
