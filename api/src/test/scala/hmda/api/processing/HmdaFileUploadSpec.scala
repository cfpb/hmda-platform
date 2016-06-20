package hmda.api.processing

import java.time.Instant

import akka.testkit.TestProbe
import hmda.api.processing.HmdaFileUpload.{ AddLine, GetState, HmdaFileUploadState }
import hmda.api.processing.HmdaFileUpload._

class HmdaFileUploadSpec extends ActorSpec {

  import hmda.parser.util.FITestData._

  val hmdaFileUpload = createHmdaFileUpload(system, "1")

  val probe = TestProbe()

  val lines = fiCSV.split("\n")
  val timestamp = Instant.now.toEpochMilli

  "A HMDA File" must {
    "be persisted" in {
      for (line <- lines) {
        probe.send(hmdaFileUpload, AddLine(timestamp, line.toString))
      }
      probe.send(hmdaFileUpload, GetState)
      probe.expectMsg(HmdaFileUploadState(Map(timestamp -> 4)))
    }

    "recover with event" in {
      probe.send(hmdaFileUpload, Shutdown)

      val secondHmdaFileUpload = createHmdaFileUpload(system, "1")

      probe.send(secondHmdaFileUpload, GetState)
      probe.expectMsg(HmdaFileUploadState(Map(timestamp -> 4)))
      probe.send(secondHmdaFileUpload, Shutdown)
    }

    "recover with from snapshot" in {
      val thirdHmdaFileUpload = createHmdaFileUpload(system, "1")
      probe.send(thirdHmdaFileUpload, CompleteUpload)
      probe.send(thirdHmdaFileUpload, Shutdown)

      Thread.sleep(500) //wait for actor messages to be processed so that the state can be saved

      val fourthHmdaFileUpload = createHmdaFileUpload(system, "1")

      probe.send(fourthHmdaFileUpload, GetState)
      probe.expectMsg(HmdaFileUploadState(Map(timestamp -> 4)))
    }
  }
}
