package hmda.persistence.institutions

import akka.testkit.TestProbe
import hmda.model.fi._
import hmda.persistence.messages.CommonMessages.GetState
import hmda.persistence.demo.DemoData
import hmda.persistence.institutions.SubmissionPersistence.{ CreateSubmission, GetSubmissionById, UpdateSubmissionStatus, _ }
import hmda.persistence.model.ActorSpec

import scala.concurrent.duration._

class SubmissionPersistenceSpec extends ActorSpec {

  val submissionsActor = createSubmissions("0", "2017", system)

  val probe = TestProbe()

  "Submissions" must {
    "be created and read back" in {
      val submissions = DemoData.testSubmissions
      for (submission <- submissions) {
        probe.send(submissionsActor, CreateSubmission)
        probe.expectMsgType[Some[Submission]]
      }
      probe.send(submissionsActor, GetState)
      val submissionState = probe.receiveOne(5.seconds)
      submissionState.asInstanceOf[Seq[Submission]].length mustBe 3
    }

    "be able to modify their status" in {
      val newStatus = Uploaded
      val id = SubmissionId("0", "2017", 1)
      probe.send(submissionsActor, UpdateSubmissionStatus(id, newStatus))
      probe.expectMsg(Some(Submission(id, newStatus)))
      probe.send(submissionsActor, GetSubmissionById(id))

      val submission = probe.receiveOne(5.seconds)
      submission.asInstanceOf[Submission].status mustBe Uploaded
    }

    "have a Signed submission update the end time" in {
      val newStatus = Signed
      val id = SubmissionId("0", "2017", 1)
      probe.send(submissionsActor, UpdateSubmissionStatus(id, newStatus))
      probe.expectMsg(Some(Submission(id, newStatus)))
      probe.send(submissionsActor, GetSubmissionById(id))

      val submission = probe.receiveOne(5.seconds)
      submission.asInstanceOf[Submission].end must not be 0L
    }
    "return None when updating nonexistent submission" in {
      val id = SubmissionId("0", "2017", 777)
      probe.send(submissionsActor, UpdateSubmissionStatus(id, Created))
      probe.expectMsg(None)
    }

    "add a filename" in {
      val id = SubmissionId("0", "2017", 2)
      probe.send(submissionsActor, GetSubmissionById(id))
      val before = probe.receiveOne(5.seconds)
      before.asInstanceOf[Submission].fileName mustBe ""

      probe.send(submissionsActor, AddSubmissionFileName(id, "my file name dot txt"))
      probe.expectMsgType[Some[Submission]]

      probe.send(submissionsActor, GetSubmissionById(id))
      val after = probe.receiveOne(5.seconds)
      after.asInstanceOf[Submission].fileName mustBe "my file name dot txt"
    }

    "return list of submissions in order of descending sequence number" in {
      probe.send(submissionsActor, GetState)
      val subs = probe.expectMsgType[List[Submission]]
      subs.head.id.sequenceNumber mustBe 3
      subs(1).id.sequenceNumber mustBe 2
      subs(2).id.sequenceNumber mustBe 1
    }
  }

}
