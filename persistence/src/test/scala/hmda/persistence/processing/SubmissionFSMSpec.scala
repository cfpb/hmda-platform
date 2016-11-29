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

      system stop fsm

    }
    "recover persisted state" in {
      val fsm = actorRef()

      probe.send(fsm, GetState)
      probe.expectMsg(NonEmptySubmissionData(Submission(submissionId, hmda.model.fi.Validated)))
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
