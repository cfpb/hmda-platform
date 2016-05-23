package hmda.api.processing

import java.time.Instant
import akka.testkit.TestProbe
import hmda.api.processing.HmdaFileUpload.{AddLine, GetState, HmdaFileUploadState}
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
  }

}
