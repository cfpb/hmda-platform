package hmda.validation.rules

import akka.NotUsed
import akka.stream._
import akka.stream.scaladsl._
import scala.concurrent.Future

trait SourceUtils {

  def count[T](input: Source[T, NotUsed])(implicit materializer: ActorMaterializer): Future[Int] = {
    input.runWith(sinkCount)
  }

  private def sinkCount[T]: Sink[T, Future[Int]] = {
    Sink.fold[Int, T](0) { (acc, _) =>
      val total = acc + 1
      total
    }
  }

}
