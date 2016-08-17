package hmda.persistence.processing

import java.time.Instant

import akka.testkit.TestProbe
import com.typesafe.config.ConfigFactory
import hmda.actor.test.ActorSpec
import hmda.persistence.CommonMessages._
import hmda.persistence.processing.HmdaRawFile._

class HmdaRawFileSpec extends ActorSpec {
  import hmda.model.util.FITestData._

  val config = ConfigFactory.load()

  val hmdaFileUpload = createHmdaRawFile(system, "1")

  val probe = TestProbe()

  val lines = fiCSV.split("\n")
  val timestamp = Instant.now.toEpochMilli

  "A HMDA File" must {
    "be persisted" in {
      for (line <- lines) {
        probe.send(hmdaFileUpload, AddLine(timestamp, line.toString))
      }
      probe.send(hmdaFileUpload, GetState)
      probe.expectMsg(HmdaRawFileState(4))
    }

    "recover with event" in {
      probe.send(hmdaFileUpload, Shutdown)

      val secondHmdaFileUpload = createHmdaRawFile(system, "1")

      probe.send(secondHmdaFileUpload, GetState)
      probe.expectMsg(HmdaRawFileState(4))
      probe.send(secondHmdaFileUpload, Shutdown)
    }
  }
}
