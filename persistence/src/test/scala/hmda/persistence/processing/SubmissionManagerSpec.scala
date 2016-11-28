package hmda.persistence.processing

import java.time.Instant

import akka.testkit.TestProbe
import com.typesafe.config.ConfigFactory
import hmda.model.fi.{ SubmissionId, Uploaded, ValidatedWithErrors }
import hmda.persistence.messages.CommonMessages.GetState
import hmda.persistence.model.ActorSpec
import hmda.persistence.processing.HmdaRawFile.AddLine
import hmda.persistence.processing.ProcessingMessages.{ CompleteUpload, StartUpload }
import hmda.persistence.processing.SubmissionManager._

class SubmissionManagerSpec extends ActorSpec {
  import hmda.model.util.FITestData._

  val config = ConfigFactory.load()

  val submissionId = SubmissionId("0", "2017", 1)

  val submissionManager = createSubmissionManager(system, submissionId)

  val probe = TestProbe()

  val lines = fiCSV.split("\n")
  val timestamp = Instant.now.toEpochMilli

  "A HMDA File" must {
    "upload, parse and validate" in {
      probe.send(submissionManager, StartUpload)
      for (line <- lines) {
        probe.send(submissionManager, AddLine(timestamp, line.toString))
      }
      probe.send(submissionManager, CompleteUpload)
      probe.send(submissionManager, GetState)
      probe.expectMsg(Uploaded)
      Thread.sleep(5000) //TODO: can this be avoided?
      probe.send(submissionManager, GetState)
      probe.expectMsg(ValidatedWithErrors)
    }

  }

}
