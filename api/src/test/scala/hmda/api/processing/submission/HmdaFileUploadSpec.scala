package hmda.api.processing.submission

import java.time.Instant

import akka.testkit.TestProbe
import hmda.api.processing.{ ActorSpec, CommonMessages }
import hmda.api.processing.submission.HmdaFileUpload._
import CommonMessages._

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
