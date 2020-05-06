package hmda.util

import akka.NotUsed
import akka.stream.Materializer
import akka.stream.scaladsl.{ Sink, Source }

import scala.collection.immutable
import scala.concurrent.Future

object SourceUtils {

  def runWithSeq[T](source: Source[T, NotUsed])(implicit mat: Materializer): Future[immutable.Seq[T]] =
    source.runWith(Sink.seq)

  def count[T](source: Source[T, NotUsed])(implicit mat: Materializer): Future[Int] =
    source.runWith(sinkCount)

  private def sinkCount[T]: Sink[T, Future[Int]] =
    Sink.fold[Int, T](0) { (acc, _) =>
      val total = acc + 1
      total
    }
}