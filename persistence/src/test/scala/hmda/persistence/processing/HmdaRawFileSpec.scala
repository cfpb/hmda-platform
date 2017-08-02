package hmda.persistence.processing

import java.time.Instant

import akka.testkit.TestProbe
import com.typesafe.config.ConfigFactory
import hmda.model.fi.SubmissionId
import hmda.persistence.messages.CommonMessages._
import hmda.persistence.model.ActorSpec
import hmda.persistence.processing.HmdaRawFile._
import hmda.persistence.processing.ProcessingMessages.Persisted

class HmdaRawFileSpec extends ActorSpec {
  import hmda.model.util.FITestData._

  val config = ConfigFactory.load()

  val submissionId = SubmissionId("0", "2017", 1)

  val hmdaFileUpload = createHmdaRawFile(system, submissionId)

  val probe = TestProbe()

  val lines = fiCSV.split("\n")
  val timestamp = Instant.now.toEpochMilli

  val fileName = "lars.dat"

  "A HMDA File" must {
    "persist file name" in {

      probe.send(hmdaFileUpload, AddFileName(fileName))
      probe.send(hmdaFileUpload, GetState)
      probe.expectMsg(HmdaRawFileState(0, fileName))
    }
    "persist raw data" in {
      for (line <- lines) {
        probe.send(hmdaFileUpload, AddLine(timestamp, line.toString))
        probe.expectMsg(Persisted)
      }
      probe.send(hmdaFileUpload, GetState)
      probe.expectMsg(HmdaRawFileState(4, fileName))
    }
    "recover with event" in {
      probe.send(hmdaFileUpload, Shutdown)

      val secondHmdaFileUpload = createHmdaRawFile(system, submissionId)

      probe.send(secondHmdaFileUpload, GetState)
      probe.expectMsg(HmdaRawFileState(4, "lars.dat"))
      probe.send(secondHmdaFileUpload, Shutdown)
    }
  }
}
