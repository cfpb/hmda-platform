package hmda.util

import akka.actor.ActorSystem
import akka.stream.Materializer
import akka.stream.scaladsl.Source
import hmda.util.SourceUtils._
import org.scalatest.{ AsyncWordSpec, MustMatchers }

class SourceUtilsSpec extends AsyncWordSpec with MustMatchers {

  implicit val system       = ActorSystem()
  implicit val materializer = Materializer(system)
  implicit val ec           = system.dispatcher

  val source1 = Source.fromIterator(() => List(1, 2, 3, 4, 5).toIterator)
  val source2 = Source.fromIterator(() => List(1, 2, 3, 4).toIterator)
  val source3 = Source.fromIterator(() => List(1, 2, 3).toIterator)

  "SourceUtils" must {
    "count elements in a Source" in {
      count(source1).map(total => total mustBe 5)
      count(source2).map(total => total mustBe 4)
      count(source3).map(total => total mustBe 3)
    }
  }

}