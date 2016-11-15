package hmda.persistence.processing

import akka.actor.ActorRef
import akka.testkit.TestProbe
import hmda.actor.test.ActorSpec
import hmda.model.fi.{ Submission, SubmissionId }
import hmda.persistence.CommonMessages.GetState
import hmda.persistence.processing.ProcessingMessages.{ CompleteUpload, StartUpload }
import hmda.persistence.processing.SubmissionFSM._

class SubmissionFSMSpec extends ActorSpec {

  val probe = TestProbe()

  val submissionId = SubmissionId("12345", "2016", 1)

  "Submission Finite State Machine" must {
    "transition through states" in {
      val fsm = actorRef()

      probe.send(fsm, Create)
      probe.send(fsm, GetState)
      probe.expectMsg(NonEmptySubmissionData(Submission(submissionId, hmda.model.fi.Created)))

      probe.send(fsm, StartUpload)
      probe.send(fsm, GetState)
      probe.expectMsg(NonEmptySubmissionData(Submission(submissionId, hmda.model.fi.Uploading)))

      probe.send(fsm, CompleteUpload)
      probe.send(fsm, GetState)
      probe.expectMsg(NonEmptySubmissionData(Submission(submissionId, hmda.model.fi.Uploaded)))

      system stop fsm

    }
    "recover persisted state" in {
      val fsm = actorRef()

      probe.send(fsm, GetState)
      probe.expectMsg(NonEmptySubmissionData(Submission(submissionId, hmda.model.fi.Uploaded)))
    }
  }

  private def actorRef(): ActorRef = createSubmissionFSM(system, submissionId)

}
