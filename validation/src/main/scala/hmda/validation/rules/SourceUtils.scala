package hmda.validation.rules

import akka.NotUsed
import akka.stream.scaladsl._
import scala.concurrent.Future
import hmda.validation._

trait SourceUtils {

  def count[T: AS: MAT](input: Source[T, NotUsed]): Future[Int] = {
    input.runWith(sinkCount)
  }

  def sum[T: AS: MAT](input: Source[T, NotUsed], summation: T => Int): Future[Int] = {
    input.runWith(sinkSum(summation))
  }

  private def sinkCount[T]: Sink[T, Future[Int]] = {
    Sink.fold[Int, T](0) { (acc, _) =>
      val total = acc + 1
      total
    }
  }

  private def sinkSum[T](summation: T => Int): Sink[T, Future[Int]] = {
    Sink.fold[Int, T](0) { (acc, lar) =>
      val total = acc + summation(lar)
      total
    }
  }

}
