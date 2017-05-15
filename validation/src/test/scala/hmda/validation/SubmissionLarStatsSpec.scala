package hmda.validation

import akka.testkit.TestProbe
import hmda.model.fi.SubmissionId
import hmda.model.fi.lar.{ LarGenerators, LoanApplicationRegister }
import hmda.persistence.messages.CommonMessages.GetState
import hmda.persistence.messages.events.processing.CommonHmdaValidatorEvents.LarValidated
import hmda.persistence.model.ActorSpec
import hmda.validation.SubmissionLarStats._
import hmda.validation.rules.lar.`macro`.{ Q070Spec, Q071Spec, Q072Spec }
import org.scalacheck.Gen

class SubmissionLarStatsSpec extends ActorSpec with LarGenerators {

  val lars10 = larListGen.sample.getOrElse(Nil)
  val lars10String = lars10.map(x => x.toCSV)

  val submissionId = SubmissionId("12345", "2017", 1)

  val submissionLarStats = createSubmissionStats(system, submissionId)

  val probe = TestProbe()

  "Submission Lar Stats" must {
    "Aggregate total submitted lar count for a submission" in {
      for (lar <- lars10String) {
        probe.send(submissionLarStats, lar)
      }
      probe.send(submissionLarStats, CountSubmittedLarsInSubmission)
      probe.send(submissionLarStats, GetState)
      val stats = probe.expectMsgType[SubmissionLarStatsState]
      stats.totalSubmitted mustBe 10
      stats.totalValidated mustBe 0
    }

    "Aggregate total verified lar count for a submission" in {
      for (lar <- lars10) {
        probe.send(submissionLarStats, LarValidated(lar, submissionId))
      }
      probe.send(submissionLarStats, PersistStatsForMacroEdits)
      probe.send(submissionLarStats, GetState)
      val stats = probe.expectMsgType[SubmissionLarStatsState]
      stats.totalSubmitted mustBe 10
      stats.totalValidated mustBe 10
    }

    "Aggregate all lars relevant to Q070" in {
      val submissionId2 = SubmissionId("12345", "2017", 2)
      val submissionLarStats2 = createSubmissionStats(system, submissionId2)

      val irrelevantLars = listOfN(11, Q070Spec.irrelevant)
      val relevantNotSoldLars = listOfN(12, Q070Spec.relevantNotSold)
      val relevantSoldLars = listOfN(13, Q070Spec.relevantSold)
      val lars = irrelevantLars ++ relevantNotSoldLars ++ relevantSoldLars

      for (lar <- lars) {
        probe.send(submissionLarStats2, LarValidated(lar, submissionId2))
      }

      probe.send(submissionLarStats2, PersistStatsForMacroEdits)
      probe.send(submissionLarStats2, GetState)
      probe.expectMsg(SubmissionLarStatsState(0, 36, 23, 13))
    }

    "Aggregate all lars relevant to Q071" in {
      val submissionId3 = SubmissionId("12345", "2017", 3)
      val submissionLarStats3 = createSubmissionStats(system, submissionId3)

      val irrelevantLars = listOfN(5, Q071Spec.irrelevant)
      val relevantNotSoldLars = listOfN(6, Q071Spec.relevantNotSold)
      val relevantSoldLars = listOfN(7, Q071Spec.relevantSold)
      val lars = irrelevantLars ++ relevantNotSoldLars ++ relevantSoldLars

      for (lar <- lars) {
        probe.send(submissionLarStats3, LarValidated(lar, submissionId3))
      }

      probe.send(submissionLarStats3, PersistStatsForMacroEdits)
      probe.send(submissionLarStats3, GetState)
      probe.expectMsg(SubmissionLarStatsState(0, 18, 0, 0, 13, 7))
    }

    "Aggregate all lars relevant to Q072" in {
      val submissionId4 = SubmissionId("12345", "2017", 4)
      val submissionLarStats4 = createSubmissionStats(system, submissionId4)

      val irrelevantLars = listOfN(9, Q072Spec.irrelevant)
      val relevantNotSoldLars = listOfN(8, Q072Spec.relevantNotSold)
      val relevantSoldLars = listOfN(7, Q072Spec.relevantSold)
      val lars = irrelevantLars ++ relevantNotSoldLars ++ relevantSoldLars

      for (lar <- lars) {
        probe.send(submissionLarStats4, LarValidated(lar, submissionId4))
      }

      probe.send(submissionLarStats4, PersistStatsForMacroEdits)
      probe.send(submissionLarStats4, GetState)
      probe.expectMsg(SubmissionLarStatsState(0, 24, 0, 0, 0, 0, 15, 7))
    }
  }

  private def listOfN(n: Int, transform: LoanApplicationRegister => LoanApplicationRegister): List[LoanApplicationRegister] = {
    larNGen(n).sample.getOrElse(List()).map(transform)
  }

}
