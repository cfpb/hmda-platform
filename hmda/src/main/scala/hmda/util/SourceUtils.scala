package hmda.util

import akka.NotUsed
import akka.stream.scaladsl.{Sink, Source}
import hmda.validation.{AS, MAT}

import scala.concurrent.Future

object SourceUtils {

  def count[T: AS: MAT](source: Source[T, NotUsed]): Future[Int] =
    source.runWith(sinkCount)

  private def sinkCount[T]: Sink[T, Future[Int]] = {
    Sink.fold[Int, T](0) { (acc, _) =>
      val total = acc + 1
      total
    }
  }

}
