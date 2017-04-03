package hmda.validation

import akka.testkit.TestProbe
import hmda.model.fi.SubmissionId
import hmda.model.fi.lar.LarGenerators
import hmda.persistence.messages.CommonMessages.GetState
import hmda.persistence.messages.events.processing.CommonHmdaValidatorEvents.LarValidated
import hmda.persistence.model.ActorSpec
import hmda.validation.ValidationStats._

class ValidationStatsSpec extends ActorSpec with LarGenerators {

  val lars10 = larListGen.sample.getOrElse(Nil)
  val submissionId = SubmissionId("12345", "2017", 1)

  val validationStats = createValidationStats(system, submissionId)

  val probe = TestProbe()

  "Validation Stats" must {
    "Aggregate total lar count for a submission" in {
      for (lar <- lars10) {
        probe.send(validationStats, LarValidated(lar, submissionId))
      }
      probe.send(validationStats, CountLarsInSubmission)
      probe.send(validationStats, GetState)
      probe.expectMsg(ValidationStatsState(10))
    }
  }

}
