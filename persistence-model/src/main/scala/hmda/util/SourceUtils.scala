package hmda.util

import akka.NotUsed
import akka.stream.scaladsl._
import hmda._

import scala.concurrent.Future

trait SourceUtils {

  def count[T: AS: MAT](input: Source[T, NotUsed]): Future[Int] = {
    input.runWith(sinkCount)
  }

  def sum[T: AS: MAT](input: Source[T, NotUsed], summation: T => Int): Future[Int] = {
    input.runWith(sinkSum(summation))
  }

  def sumDouble[T: AS: MAT](input: Source[T, NotUsed], summation: T => Double): Future[Double] = {
    input.runWith(sinkSumDouble(summation))
  }

  def collectHeadValue[T: AS: MAT: EC](input: Source[T, NotUsed]): Future[T] = {
    input.take(1).runWith(Sink.seq).map(xs => xs.head)
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

  private def sinkSumDouble[T](summation: T => Double): Sink[T, Future[Double]] = {
    Sink.fold[Double, T](0) { (acc, lar) =>
      val total = acc + summation(lar)
      total
    }
  }
}
