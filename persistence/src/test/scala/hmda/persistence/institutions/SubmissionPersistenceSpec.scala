package hmda.persistence.institutions

import akka.testkit.{ EventFilter, TestProbe }
import hmda.actor.test.ActorSpec
import hmda.model.fi._
import hmda.persistence.CommonMessages.GetState
import hmda.persistence.demo.DemoData
import hmda.persistence.institutions.SubmissionPersistence.{ CreateSubmission, GetSubmissionById, UpdateSubmissionStatus, _ }

class SubmissionPersistenceSpec extends ActorSpec {

  val submissionsActor = createSubmissions("12345", "2017", system)

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
      val id = 1
      probe.send(submissionsActor, UpdateSubmissionStatus(1, newStatus))
      probe.send(submissionsActor, GetSubmissionById(1))
      probe.expectMsg(Submission(1, Uploaded))
    }
  }

  "Error logging" must {
    "warn when updating nonexistent submission" in {
      val id = 777
      val msg = s"Submission does not exist. Could not update submission with id $id"
      EventFilter.warning(message = msg, occurrences = 1) intercept {
        probe.send(submissionsActor, UpdateSubmissionStatus(id, Created))
      }
    }
  }

}
