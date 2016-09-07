package hmda.persistence.institutions

import akka.testkit.{ EventFilter, TestProbe }
import hmda.actor.test.ActorSpec
import hmda.model.fi._
import hmda.persistence.CommonMessages.GetState
import hmda.persistence.demo.DemoData
import hmda.persistence.institutions.SubmissionPersistence.{ CreateSubmission, GetSubmissionById, UpdateSubmissionStatus, _ }

class SubmissionPersistenceSpec extends ActorSpec {
  val institutionId = "0"
  val period = "2017"

  val submissionsActor = createSubmissions(institutionId, period, system)

  val probe = TestProbe()

  "Submissions" must {
    "be created and read back" in {
      val submissions = DemoData.testSubmissions
      for (submission <- submissions) {
        probe.send(submissionsActor, CreateSubmission)
      }
      probe.send(submissionsActor, GetState)
      probe.expectMsg(DemoData.testSubmissions.reverse)
    }

    "be able to modify their status" in {
      val newStatus = Uploaded
      val seqNo = 1
      val submissionId = SubmissionId(institutionId, period, seqNo)
      probe.send(submissionsActor, UpdateSubmissionStatus(submissionId, newStatus))
      probe.send(submissionsActor, GetSubmissionById(submissionId))
      probe.expectMsg(Submission(submissionId, Uploaded))
    }
  }

  "Error logging" must {
    "warn when updating nonexistent submission" in {
      val seqNo = 777
      val submissionId = SubmissionId(institutionId, period, seqNo)
      val msg = s"Submission does not exist. Could not update submission with id ${submissionId.toString}"
      EventFilter.warning(message = msg, occurrences = 1) intercept {
        probe.send(submissionsActor, UpdateSubmissionStatus(submissionId, Created))
      }
    }
  }

}
