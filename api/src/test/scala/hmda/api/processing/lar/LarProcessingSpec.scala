package hmda.api.processing.lar

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import hmda.parser.fi.lar.LarGenerators
import org.scalatest.{ MustMatchers, PropSpec }
import org.scalatest.prop.PropertyChecks
import hmda.api.processing.lar.LarProcessing._

import scala.concurrent.Await
import scala.concurrent.duration._

class LarProcessingSpec extends PropSpec with PropertyChecks with MustMatchers with LarGenerators {
  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()

  property("A list of LARs must be parsed") {
    forAll(larListGen) { larList =>
      val input = Source.fromIterator(() => larList.toIterator).map(lar => lar.toCSV)
      val fresult = input.via(parseLars).runWith(larsSink)
      val lars = Await.result(fresult, 5.seconds)
      lars mustBe larList
    }
  }
}
