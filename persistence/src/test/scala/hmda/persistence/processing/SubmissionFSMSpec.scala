package hmda.persistence.processing

import akka.actor.ActorRef
import akka.testkit.TestProbe
import hmda.model.fi.{ Submission, SubmissionId }
import hmda.persistence.messages.CommonMessages.GetState
import hmda.persistence.model.ActorSpec
import hmda.persistence.processing.ProcessingMessages._
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

      probe.send(fsm, StartParsing)
      probe.send(fsm, GetState)
      probe.expectMsg(NonEmptySubmissionData(Submission(submissionId, hmda.model.fi.Parsing)))

      probe.send(fsm, CompleteParsing)
      probe.send(fsm, GetState)
      probe.expectMsg(NonEmptySubmissionData(Submission(submissionId, hmda.model.fi.Parsed)))

      probe.send(fsm, BeginValidation(probe.testActor))
      probe.send(fsm, GetState)
      probe.expectMsg(NonEmptySubmissionData(Submission(submissionId, hmda.model.fi.Validating)))

      probe.send(fsm, CompleteValidation(probe.testActor))
      probe.send(fsm, GetState)
      probe.expectMsg(NonEmptySubmissionData(Submission(submissionId, hmda.model.fi.Validated)))

      probe.send(fsm, Sign)
      probe.expectMsg(Some(hmda.model.fi.Signed))
      probe.send(fsm, GetState)
      probe.expectMsg(NonEmptySubmissionData(Submission(submissionId, hmda.model.fi.Signed)))

      system stop fsm

    }
    "respond with None for invalid state transition" in {
      val subId = SubmissionId("instId", "period", 4)
      val fsm = createSubmissionFSM(system, subId)

      probe.send(fsm, Create)
      probe.send(fsm, GetState)
      probe.expectMsg(NonEmptySubmissionData(Submission(subId, hmda.model.fi.Created)))

      // Cannot sign a submission that isn't in state Validated or ValidatedWithErrors
      probe.send(fsm, Sign)
      probe.expectMsg(None)

      probe.send(fsm, GetState)
      probe.expectMsg(NonEmptySubmissionData(Submission(subId, hmda.model.fi.Created)))

      system stop fsm
    }
    "recover persisted state" in {
      val fsm = actorRef()

      probe.send(fsm, GetState)
      probe.expectMsg(NonEmptySubmissionData(Submission(submissionId, hmda.model.fi.Signed)))
    }

    "fail a submission" in {
      val fsm = actorRef()

      probe.send(fsm, None)
      probe.send(fsm, GetState)
      probe.expectMsg(NonEmptySubmissionData(Submission(submissionId, hmda.model.fi.Failed(SubmissionFSM.failedMsg))))
    }
  }

  private def actorRef(): ActorRef = createSubmissionFSM(system, submissionId)

}
