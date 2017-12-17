package hmda.validation.stats

import java.time.Instant

import akka.testkit.TestProbe
import hmda.census.model.Msa
import hmda.model.fi.SubmissionId
import hmda.model.fi.lar.{ LarGenerators, LoanApplicationRegister }
import hmda.persistence.messages.CommonMessages.GetState
import hmda.persistence.messages.events.processing.CommonHmdaValidatorEvents.LarValidated
import hmda.persistence.messages.events.processing.FileUploadEvents.LineAdded
import hmda.persistence.model.{ ActorSpec, MsaGenerators }
import hmda.validation.messages.ValidationStatsMessages._
import hmda.validation.stats.ValidationStats._
import hmda.validation.stats.SubmissionLarStats._

class ValidationStatsSpec extends ActorSpec with MsaGenerators with LarGenerators {

  val probe = TestProbe()

  val submissionValidationStats = createValidationStats(system)

  val id1 = SubmissionId("12345", "2017", 1)
  val id2 = SubmissionId("12345", "2017", 2)
  val id3 = SubmissionId("12345", "2016", 1)

  val larStats1 = createSubmissionStats(system, id1)
  val larStats2 = createSubmissionStats(system, id2)
  val larStats3 = createSubmissionStats(system, id3)

  val msa = listOfMsaGen.sample.getOrElse(List[Msa]())
  //
  //  "Submission Validation Stats" must {
  //    "Add submission stats" in {
  //
  //      val s1 = SubmissionStats(id1, 20, 21, 22, 23, 24, 25, 26, 27, .28, .29, "a", msa)
  //      val s2 = SubmissionStats(id2, 124, 125, 126, 127, 128, 129, 130, 131, .132, .133, "b", List[Msa]())
  //      val s3 = SubmissionStats(id3, 101, 100, 99, 98, 97, 96, 95, 94, .93, .92, "c", List[Msa]())
  //      probe.send(submissionValidationStats, AddSubmissionSubmittedTotal(20, id1))
  //      probe.send(submissionValidationStats, AddSubmissionMacroStats(id1, 21, 22, 23, 24, 25, 26, 27, 0.28, 0.29))
  //      probe.send(submissionValidationStats, AddSubmissionTaxId("a", id1))
  //      probe.send(submissionValidationStats, AddIrsStats(msa, id1))
  //
  //      probe.send(submissionValidationStats, AddSubmissionSubmittedTotal(124, id2))
  //      probe.send(submissionValidationStats, AddSubmissionMacroStats(id2, 125, 126, 127, 128, 129, 130, 131, 0.132, 0.133))
  //      probe.send(submissionValidationStats, AddSubmissionTaxId("b", id2))
  //
  //      probe.send(submissionValidationStats, AddSubmissionSubmittedTotal(101, id3))
  //      probe.send(submissionValidationStats, AddSubmissionMacroStats(id3, 100, 99, 98, 97, 96, 95, 94, .93, .92))
  //      probe.send(submissionValidationStats, AddSubmissionTaxId("c", id3))
  //
  //      probe.send(submissionValidationStats, GetState)
  //      probe.expectMsg(ValidationStatsState(Seq(s1, s2, s3)))
  //    }
  //
  "Find total submitted lars for an institution in a certain period" in {
    probe.send(submissionValidationStats, AddSubmissionLarStatsActorRef(larStats1, id1))
    for (_ <- 1 to 124) {
      probe.send(larStats1, LineAdded(Instant.now.getEpochSecond, "lar"))
    }
    probe.send(larStats1, CountSubmittedLarsInSubmission)
    probe.send(submissionValidationStats, FindTotalSubmittedLars(id1.institutionId, id1.period))
    probe.expectMsg(124)

  }
  "Find latest total submitted lars for an institution in a certain period" in {
    probe.send(submissionValidationStats, AddSubmissionLarStatsActorRef(larStats2, id2))
    for (_ <- 1 to 101) {
      probe.send(larStats2, LineAdded(Instant.now.getEpochSecond, "lar"))
    }
    probe.send(larStats2, CountSubmittedLarsInSubmission)
    probe.send(submissionValidationStats, FindTotalSubmittedLars(id1.institutionId, id1.period))
    probe.expectMsg(101)
  }

  "Find total validated lars for an institution in a certain period" in {
    probe.send(submissionValidationStats, AddSubmissionLarStatsActorRef(larStats3, id3))
    for (_ <- 1 to 100) {
      probe.send(larStats3, LarValidated(larGen.sample.getOrElse(LoanApplicationRegister()), id3))
    }
    probe.send(submissionValidationStats, FindTotalValidatedLars(id3.institutionId, id3.period))
    probe.expectMsg(100)

    for (_ <- 1 to 125) {
      probe.send(larStats2, LarValidated(larGen.sample.getOrElse(LoanApplicationRegister()), id2))
    }
    probe.send(submissionValidationStats, FindTotalValidatedLars(id2.institutionId, id2.period))
    probe.expectMsg(125)

  }

  "Find tax ID for an institution in a certain period" in {
    probe.send(submissionValidationStats, AddSubmissionTaxId("a", id1))
    probe.send(submissionValidationStats, FindTaxId(id1.institutionId, id1.period))
    probe.expectMsg("a")
    probe.send(submissionValidationStats, AddSubmissionTaxId("c", id3))
    probe.send(submissionValidationStats, FindTaxId(id3.institutionId, id3.period))
    probe.expectMsg("c")
  }
  //
  //    "Find IRS stats for an institution in a certain submission" in {
  //      probe.send(submissionValidationStats, FindIrsStats(SubmissionId("12345", "2017", 1)))
  //      probe.expectMsg(msa)
  //      probe.send(submissionValidationStats, FindIrsStats(SubmissionId("12345", "2017", 2)))
  //      probe.expectMsg(List[Msa]())
  //      probe.send(submissionValidationStats, FindIrsStats(SubmissionId("12345", "2016", 1)))
  //      probe.expectMsg(List[Msa]())
  //    }
  //
  //    "Find Q070 stats" in {
  //      probe.send(submissionValidationStats, FindQ070("12345", "2017"))
  //      probe.expectMsg((126, 127))
  //      probe.send(submissionValidationStats, FindQ070("12345", "2016"))
  //      probe.expectMsg((99, 98))
  //      probe.send(submissionValidationStats, FindQ070("nonexistent", "bogus"))
  //      probe.expectMsg((0, 0))
  //    }
  //
  //    "Find Q071 stats" in {
  //      probe.send(submissionValidationStats, FindQ071("12345", "2017"))
  //      probe.expectMsg((128, 129))
  //      probe.send(submissionValidationStats, FindQ071("12345", "2016"))
  //      probe.expectMsg((97, 96))
  //      probe.send(submissionValidationStats, FindQ071("nonexistent", "bogus"))
  //      probe.expectMsg((0, 0))
  //    }
  //
  //    "Find Q072 stats" in {
  //      probe.send(submissionValidationStats, FindQ072("12345", "2017"))
  //      probe.expectMsg((130, 131))
  //      probe.send(submissionValidationStats, FindQ072("12345", "2016"))
  //      probe.expectMsg((95, 94))
  //      probe.send(submissionValidationStats, FindQ072("nonexistent", "bogus"))
  //      probe.expectMsg((0, 0))
  //    }
  //
  //    "Find Q075 stats" in {
  //      probe.send(submissionValidationStats, FindQ075("12345", "2017"))
  //      probe.expectMsg(0.132)
  //      probe.send(submissionValidationStats, FindQ075("12345", "2016"))
  //      probe.expectMsg(0.93)
  //      probe.send(submissionValidationStats, FindQ075("nonexistent", "bogus"))
  //      probe.expectMsg(0.0)
  //    }
  //
  //    "Find Q076 stats" in {
  //      probe.send(submissionValidationStats, FindQ076("12345", "2017"))
  //      probe.expectMsg(0.133)
  //      probe.send(submissionValidationStats, FindQ076("12345", "2016"))
  //      probe.expectMsg(0.92)
  //      probe.send(submissionValidationStats, FindQ076("nonexistent", "bogus"))
  //      probe.expectMsg(0.0)
  //    }
  //  }
  //
}
