package hmda.validation

import akka.testkit.TestProbe
import hmda.model.fi.SubmissionId
import hmda.model.fi.lar.LarGenerators
import hmda.persistence.messages.CommonMessages.GetState
import hmda.persistence.messages.events.processing.CommonHmdaValidatorEvents.LarValidated
import hmda.persistence.model.ActorSpec
import hmda.validation.SubmissionLarStats._

class SubmissionLarStatsSpec extends ActorSpec with LarGenerators {

  val lars10 = larListGen.sample.getOrElse(Nil)

  val submissionId = SubmissionId("12345", "2017", 1)

  val submissionLarStats = createSubmissionStats(system, submissionId)

  val probe = TestProbe()

  "Submission Lar Stats" must {
    "Aggregate total lar count for a submission" in {
      for (lar <- lars10) {
        probe.send(submissionLarStats, LarValidated(lar, submissionId))
      }
      probe.send(submissionLarStats, CountLarsInSubmission)
      probe.send(submissionLarStats, GetState)
      probe.expectMsg(SubmissionLarStatsState(10))
    }
  }
}
