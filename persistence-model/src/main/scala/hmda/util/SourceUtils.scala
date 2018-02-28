package hmda.util

import akka.NotUsed
import akka.stream.scaladsl._
import hmda._

import scala.concurrent.Future
import scala.util.Try

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

  def collectHeadValue[T: AS: MAT: EC](input: Source[T, NotUsed]): Future[Try[T]] = {
    input.take(1).runWith(Sink.seq).map(xs => Try(xs.head))
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

  def calculateMean[ec: EC, mat: MAT, as: AS, T](source: Source[T, NotUsed], f: T => Double): Future[Double] = {
    val loanCountF = count(source)
    val valueSumF = sumDouble(source, f)

    for {
      count <- loanCountF
      totalRateSpread <- valueSumF
    } yield {
      if (count == 0) 0
      else {
        val v = totalRateSpread / count
        BigDecimal(v).setScale(2, BigDecimal.RoundingMode.HALF_UP).toDouble
      }
    }
  }

  def calculateMedian[ec: EC, mat: MAT, as: AS, T](source: Source[T, NotUsed], f: T => Double): Future[Double] = {
    // If this method encounters collections that are too large and overload memory,
    //   add this to the statement below, with a reasonable limit:
    //   .limit(MAX_SIZE)
    val valuesF: Future[Seq[Double]] = source.map(f).runWith(Sink.seq)

    valuesF.map { seq =>
      if (seq.isEmpty) 0 else calculateMedian(seq)
    }
  }

  def calculateMedian(seq: Seq[Double]): Double = {
    val (lowerHalf, upperHalf) = seq.sortWith(_ < _).splitAt(seq.size / 2)
    val median = if (seq.size % 2 == 0) (lowerHalf.last + upperHalf.head) / 2.0 else upperHalf.head
    BigDecimal(median).setScale(2, BigDecimal.RoundingMode.HALF_UP).toDouble
  }

}
