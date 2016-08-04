package hmda.persistence.processing

import java.time.Instant

import akka.NotUsed
import scala.concurrent._
import scala.concurrent.duration._
import akka.pattern.ask
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{ Sink, Source }
import akka.util.Timeout
import akka.testkit.TestProbe
import com.typesafe.config.ConfigFactory
import hmda.actor.test.ActorSpec
import hmda.model.util.FITestData._
import hmda.persistence.CommonMessages.Event
import hmda.persistence.processing.HmdaRawFile._
import hmda.persistence.processing.HmdaRawFileQuery._

class HmdaRawFileQuerySpec extends ActorSpec {

  implicit val timeout: Timeout = Timeout(5.seconds)

  implicit val materializer = ActorMaterializer()

  val submissionId = "12345-2017-1"
  val config = ConfigFactory.load()
  val hmdaRawFile = createHmdaRawFile(system, submissionId)
  val hmdaRawFileQuery = createHmdaRawFileQuery(system)
  val probe = TestProbe()

  val lines = fiCSV.split("\n")
  val timestamp = Instant.now.toEpochMilli

  "HMDARawFileQuery" must {
    "read raw data from journal" in {
      for (line <- lines) {
        probe.send(hmdaRawFile, AddLine(timestamp, line.toString))
      }
      val eventsF = (hmdaRawFileQuery ? ReadHmdaRawData(submissionId))
        .mapTo[Source[Event, NotUsed]]

      val sink = Sink.foreach(println)

      val events = Await.result(eventsF, 5.seconds)
      events.runWith(sink)

      1 === 1
    }
  }

}
