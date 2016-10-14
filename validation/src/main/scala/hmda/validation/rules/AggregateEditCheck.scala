package hmda.validation.rules

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{ Sink, Source }
import hmda.validation.dsl.Result
import scala.concurrent.{ ExecutionContext, Future }

abstract class AggregateEditCheck[A <: Source[T, NotUsed], T] {

  implicit val system: ActorSystem = ActorSystem("macro-edits-system")

  implicit val ec: ExecutionContext = system.dispatcher

  implicit val materializer: ActorMaterializer = ActorMaterializer()

  def count(input: Source[T, NotUsed]): Future[Double] = {
    input.runWith(sinkCount)
  }

  private def sinkCount: Sink[T, Future[Double]] = {
    Sink.fold[Double, T](0) { (acc, _) =>
      val total = acc + 1
      total.toDouble
    }
  }

  def name: String

  def apply(input: A): Future[Result]

}
