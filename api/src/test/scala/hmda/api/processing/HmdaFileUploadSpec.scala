package hmda.api.processing

import java.time.Instant
import hmda.api.processing.HmdaFileUpload.{ AddLine, GetState, HmdaFileUploadState }

class HmdaFileUploadSpec extends ActorSpec {

  import hmda.parser.util.FITestData._

  val hmdaFileUpload = system.actorOf(HmdaFileUpload.props("1"))

  val lines = fiCSV.split("\n")
  val timestamp = Instant.now.toEpochMilli

  "A HMDA File" must {
    "be persisted" in {
      for (line <- lines) {
        hmdaFileUpload ! AddLine(timestamp, line.toString)
      }
      hmdaFileUpload ! GetState
      expectMsg(HmdaFileUploadState(Map(timestamp -> 4)))
    }
  }

}
