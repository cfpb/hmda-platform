package hmda.api.persistence

import akka.testkit.TestProbe
import hmda.api.demo.DemoData
import hmda.api.persistence.CommonMessages.GetState
import hmda.api.processing.ActorSpec
import hmda.api.persistence.SubmissionPersistence._
import hmda.model.fi.{ Submission, Uploaded }

class SubmissionPersistenceSpec extends ActorSpec {

  val submissionsActor = createSubmissions("12345", "2017", system)

  val probe = TestProbe()

  "Submissions" must {
    "be created and read back" in {
      val submissions = DemoData.newSubmissions
      for (submission <- submissions) {
        probe.send(submissionsActor, CreateSubmission)
      }
      probe.send(submissionsActor, GetState)
      probe.expectMsg(DemoData.newSubmissions.reverse)
    }

    "be able to modify their status" in {
      val newStatus = Uploaded
      val id = 1
      probe.send(submissionsActor, UpdateSubmissionStatus(1, newStatus))
      probe.send(submissionsActor, GetSubmissionById(1))
      probe.expectMsg(Submission(1, Uploaded))
    }
  }

}
