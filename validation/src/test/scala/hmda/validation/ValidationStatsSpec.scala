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
      val id1 = SubmissionId("12345", "2017", 1)
      val id2 = SubmissionId("12345", "2017", 2)
      val id3 = SubmissionId("12345", "2016", 1)
      val s1 = SubmissionStats(id1, 99, 100, "a")
      val s2 = SubmissionStats(id2, 124, 125, "b")
      val s3 = SubmissionStats(id3, 101, 100, "c")
      probe.send(submissionValidationStats, AddSubmissionSubmittedTotal(99, id1))
      probe.send(submissionValidationStats, AddSubmissionValidationTotal(100, id1))
      probe.send(submissionValidationStats, AddSubmissionTaxId("a", id1))
      probe.send(submissionValidationStats, AddSubmissionSubmittedTotal(124, id2))
      probe.send(submissionValidationStats, AddSubmissionValidationTotal(125, id2))
      probe.send(submissionValidationStats, AddSubmissionTaxId("b", id2))
      probe.send(submissionValidationStats, AddSubmissionSubmittedTotal(101, id3))
      probe.send(submissionValidationStats, AddSubmissionValidationTotal(100, id3))
      probe.send(submissionValidationStats, AddSubmissionTaxId("c", id3))

      probe.send(submissionValidationStats, GetState)
      probe.expectMsg(ValidationStatsState(Seq(s1, s2, s3)))
    }

    "Find total submitted lars for an institution in a certain period" in {
      probe.send(submissionValidationStats, FindTotalSubmittedLars("12345", "2016"))
      probe.expectMsg(101)
      probe.send(submissionValidationStats, FindTotalSubmittedLars("12345", "2017"))
      probe.expectMsg(124)
    }

    "Find total verified lars for an institution in a certain period" in {
      probe.send(submissionValidationStats, FindTotalValidatedLars("12345", "2016"))
      probe.expectMsg(150)
      probe.send(submissionValidationStats, FindTotalValidatedLars("12345", "2017"))
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
