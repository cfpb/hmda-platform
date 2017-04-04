package hmda.validation

import akka.testkit.TestProbe
import hmda.model.fi.SubmissionId
import hmda.persistence.messages.CommonMessages.GetState
import hmda.persistence.model.ActorSpec
import hmda.validation.ValidationStats._

class ValidationStatsSpec extends ActorSpec {

  val probe = TestProbe()

  val submissionValidationStats = createValidationStats(system)

  "Submission Validation Stats" must {
    "Add submission stats" in {
      val s1 = SubmissionStats(SubmissionId("12345", "2017", 1), 100)
      val s2 = SubmissionStats(SubmissionId("12345", "2017", 2), 125)
      val s3 = SubmissionStats(SubmissionId("12345", "2016", 1), 100)
      probe.send(submissionValidationStats, AddSubmissionStats(s1))
      probe.send(submissionValidationStats, AddSubmissionStats(s2))
      probe.send(submissionValidationStats, AddSubmissionStats(s3))
      probe.send(submissionValidationStats, GetState)
      probe.expectMsg(ValidationStatsState(Seq(s1, s2, s3)))
    }
    "Find total lars for an institution in a certain period" in {
      probe.send(submissionValidationStats, FindTotalLars("12345", "2016"))
      probe.expectMsg(100)
      probe.send(submissionValidationStats, FindTotalLars("12345", "2017"))
      probe.expectMsg(125)
    }
  }

}
