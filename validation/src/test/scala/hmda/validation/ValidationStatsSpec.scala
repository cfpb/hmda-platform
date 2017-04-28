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
      val s1 = SubmissionStats(SubmissionId("12345", "2017", 1), 200, 100, "a")
      val s2 = SubmissionStats(SubmissionId("12345", "2017", 2), 250, 125, "b")
      val s3 = SubmissionStats(SubmissionId("12345", "2016", 1), 300, 150, "c")
      probe.send(submissionValidationStats, AddSubmissionStats(s1))
      probe.send(submissionValidationStats, AddSubmissionStats(s2))
      probe.send(submissionValidationStats, AddSubmissionStats(s3))
      probe.send(submissionValidationStats, GetState)
      probe.expectMsg(ValidationStatsState(Seq(s1, s2, s3)))
    }
    "Find total verified lars for an institution in a certain period" in {
      probe.send(submissionValidationStats, FindTotalVerifiedLars("12345", "2016"))
      probe.expectMsg(150)
      probe.send(submissionValidationStats, FindTotalVerifiedLars("12345", "2017"))
      probe.expectMsg(125)
    }

    "Find total submitted lars for an institution in a certain period" in {
      probe.send(submissionValidationStats, FindTotalSubmittedLars("12345", "2016"))
      probe.expectMsg(300)
      probe.send(submissionValidationStats, FindTotalSubmittedLars("12345", "2017"))
      probe.expectMsg(250)
    }

    "Find tax ID for an institution in a certain period" in {
      probe.send(submissionValidationStats, FindTaxId("12345", "2017"))
      probe.expectMsg("b")
      probe.send(submissionValidationStats, FindTaxId("12345", "2016"))
      probe.expectMsg("c")
    }
  }

}
