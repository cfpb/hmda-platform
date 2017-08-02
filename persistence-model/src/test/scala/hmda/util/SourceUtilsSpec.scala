package hmda.util

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import org.scalatest.{ AsyncWordSpec, BeforeAndAfterAll, MustMatchers }

class SourceUtilsSpec extends AsyncWordSpec with MustMatchers with BeforeAndAfterAll with SourceUtils {

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  implicit val ec = system.dispatcher

  val l5 = List(1, 2, 3, 4, 5)
  val l4 = List(1, 2, 3, 4)
  val l3 = List(1, 2, 3)

  "SourceUtils" must {
    "count the elements in a Source" in {
      val s5 = Source.fromIterator(() => l5.toIterator)
      val s4 = Source.fromIterator(() => l4.toIterator)
      val s3 = Source.fromIterator(() => l3.toIterator)
      count(s5).map(total => total mustBe 5)
      count(s4).map(total => total mustBe 4)
      count(s3).map(total => total mustBe 3)
    }

    "sum the elements in a Source" in {
      def itself(int: Int) = int
      val s5 = Source.fromIterator(() => l5.toIterator)
      val s4 = Source.fromIterator(() => l4.toIterator)
      val s3 = Source.fromIterator(() => l3.toIterator)
      sum(s5, itself).map(total => total mustBe 15)
      sum(s4, itself).map(total => total mustBe 10)
      sum(s3, itself).map(total => total mustBe 6)
    }

  }

  override def afterAll(): Unit = {
    system.terminate()
  }

}
