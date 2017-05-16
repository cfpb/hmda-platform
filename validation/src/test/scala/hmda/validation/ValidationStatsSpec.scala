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
      val s1 = SubmissionStats(id1, 20, 21, 22, 23, 24, 25, 26, 27, "a")
      val s2 = SubmissionStats(id2, 124, 125, 126, 127, 128, 129, 130, 131, "b")
      val s3 = SubmissionStats(id3, 101, 100, 99, 98, 97, 96, 95, 94, "c")
      probe.send(submissionValidationStats, AddSubmissionSubmittedTotal(20, id1))
      probe.send(submissionValidationStats, AddSubmissionMacroStats(id1, 21, 22, 23, 24, 25, 26, 27))
      probe.send(submissionValidationStats, AddSubmissionTaxId("a", id1))

      probe.send(submissionValidationStats, AddSubmissionSubmittedTotal(124, id2))
      probe.send(submissionValidationStats, AddSubmissionMacroStats(id2, 125, 126, 127, 128, 129, 130, 131))
      probe.send(submissionValidationStats, AddSubmissionTaxId("b", id2))

      probe.send(submissionValidationStats, AddSubmissionSubmittedTotal(101, id3))
      probe.send(submissionValidationStats, AddSubmissionMacroStats(id3, 100, 99, 98, 97, 96, 95, 94))
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

    "Find total validated lars for an institution in a certain period" in {
      probe.send(submissionValidationStats, FindTotalValidatedLars("12345", "2016"))
      probe.expectMsg(100)
      probe.send(submissionValidationStats, FindTotalValidatedLars("12345", "2017"))
      probe.expectMsg(125)
    }

    "Find tax ID for an institution in a certain period" in {
      probe.send(submissionValidationStats, FindTaxId("12345", "2017"))
      probe.expectMsg("b")
      probe.send(submissionValidationStats, FindTaxId("12345", "2016"))
      probe.expectMsg("c")
    }

    "Find Q070 stats" in {
      probe.send(submissionValidationStats, FindQ070("12345", "2017"))
      probe.expectMsg((126, 127))
      probe.send(submissionValidationStats, FindQ070("12345", "2016"))
      probe.expectMsg((99, 98))
      probe.send(submissionValidationStats, FindQ070("nonexistent", "bogus"))
      probe.expectMsg((0, 0))
    }

    "Find Q071 stats" in {
      probe.send(submissionValidationStats, FindQ071("12345", "2017"))
      probe.expectMsg((128, 129))
      probe.send(submissionValidationStats, FindQ071("12345", "2016"))
      probe.expectMsg((97, 96))
      probe.send(submissionValidationStats, FindQ071("nonexistent", "bogus"))
      probe.expectMsg((0, 0))
    }

    "Find Q072 stats" in {
      probe.send(submissionValidationStats, FindQ072("12345", "2017"))
      probe.expectMsg((130, 131))
      probe.send(submissionValidationStats, FindQ072("12345", "2016"))
      probe.expectMsg((95, 94))
      probe.send(submissionValidationStats, FindQ072("nonexistent", "bogus"))
      probe.expectMsg((0, 0))
    }

    "Find Q075 stats" in {
      probe.send(submissionValidationStats, FindQ075("12345", "2017"))
      probe.expectMsg((128, 129))
      probe.send(submissionValidationStats, FindQ075("12345", "2016"))
      probe.expectMsg((97, 96))
      probe.send(submissionValidationStats, FindQ075("nonexistent", "bogus"))
      probe.expectMsg((0, 0))
    }
  }

}
