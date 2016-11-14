package hmda.persistence.institutions

import akka.testkit.{ EventFilter, TestProbe }
import hmda.actor.test.ActorSpec
import hmda.model.fi._
import hmda.persistence.CommonMessages.GetState
import hmda.persistence.demo.DemoData
import hmda.persistence.institutions.SubmissionPersistence.{ CreateSubmission, GetSubmissionById, UpdateSubmissionStatus, _ }
import org.scalatest.Matchers

import scala.concurrent.duration._

class SubmissionPersistenceSpec extends ActorSpec {

  val submissionsActor = createSubmissions("0", "2017", system)

  val probe = TestProbe()

  "Submissions" must {
    "be created and read back" in {
      val submissions = DemoData.testSubmissions
      for (submission <- submissions) {
        probe.send(submissionsActor, CreateSubmission)
      }
      probe.send(submissionsActor, GetState)
      val submissionState = probe.receiveOne(5.seconds)
      submissionState.asInstanceOf[Seq[Submission]].length mustBe 3
    }

    "be able to modify their status" in {
      val newStatus = Uploaded
      val id = SubmissionId("0", "2017", 1)
      probe.send(submissionsActor, UpdateSubmissionStatus(id, newStatus))
      probe.send(submissionsActor, GetSubmissionById(id))

      val submission = probe.receiveOne(5.seconds)
      submission.asInstanceOf[Submission].status mustBe Uploaded
    }

    "have a Signed submission update the end time" in {
      val newStatus = Signed
      val id = SubmissionId("0", "2017", 1)
      probe.send(submissionsActor, UpdateSubmissionStatus(id, newStatus))
      probe.send(submissionsActor, GetSubmissionById(id))

      val submission = probe.receiveOne(5.seconds)
      submission.asInstanceOf[Submission].end must not be 0L
    }
  }

  "Error logging" must {
    "warn when updating nonexistent submission" in {
      val id = SubmissionId("0", "2017", 777)
      val msg = s"Submission does not exist. Could not update submission with id $id"
      EventFilter.warning(message = msg, occurrences = 1) intercept {
        probe.send(submissionsActor, UpdateSubmissionStatus(id, Created))
      }
    }
  }

}
