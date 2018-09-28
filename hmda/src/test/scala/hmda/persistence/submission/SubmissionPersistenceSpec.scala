package hmda.persistence.submission

import akka.actor
import akka.actor.testkit.typed.scaladsl.TestProbe
import hmda.persistence.AkkaCassandraPersistenceSpec
import akka.actor.typed.scaladsl.adapter._
import hmda.messages.submission.SubmissionCommands.{
  CreateSubmission,
  GetSubmission,
  ModifySubmission
}
import hmda.messages.submission.SubmissionEvents.{
  SubmissionCreated,
  SubmissionEvent,
  SubmissionModified,
  SubmissionNotExists
}
import hmda.model.filing.submission.{
  Created,
  Submission,
  SubmissionId,
  Uploaded
}
import hmda.model.submission.SubmissionGenerator._

import scala.concurrent.duration._

class SubmissionPersistenceSpec extends AkkaCassandraPersistenceSpec {
  override implicit val system = actor.ActorSystem()
  override implicit val typedSystem = system.toTyped

  val submissionProbe = TestProbe[SubmissionEvent]("submission-probe")
  val maybeSubmissionProbe =
    TestProbe[Option[Submission]]("submission-get0-probe")
  val sampleSubmission = submissionGen
    .suchThat(s => !s.id.isEmpty)
    .suchThat(s => s.id.lei != "" && s.id.lei != "AA")
    .suchThat(s => s.status == Created)
    .sample
    .getOrElse(Submission(SubmissionId("12345", "2018", 1)))

  val modified = sampleSubmission.copy(status = Uploaded)

  val submissionId = sampleSubmission.id

  "A Submission" must {
    "be created and read back" in {
      val submissionPersistence =
        system.spawn(SubmissionPersistence.behavior(submissionId.toString),
                     actorName)
      submissionPersistence ! CreateSubmission(sampleSubmission.id,
                                               submissionProbe.ref)
      submissionProbe.expectMessageType[SubmissionCreated]

      submissionPersistence ! GetSubmission(maybeSubmissionProbe.ref)
      maybeSubmissionProbe.expectMessageType[Option[Submission]]

      maybeSubmissionProbe.within(0.seconds, 5.seconds) {
        msg: Option[Submission] =>
          msg match {
            case Some(_) => succeed
            case None    => fail
          }
      }
    }

    "be modified and read back" in {
      val submissionPersistence =
        system.spawn(SubmissionPersistence.behavior(submissionId.toString),
                     actorName)
      submissionPersistence ! CreateSubmission(sampleSubmission.id,
                                               submissionProbe.ref)
      submissionProbe.expectMessageType[SubmissionCreated]

      submissionPersistence ! ModifySubmission(modified, submissionProbe.ref)
      submissionProbe.expectMessage(SubmissionModified(modified))
    }

    "return not exists message if trying to modify submission that doesn't exist" in {
      val submissionPersistence =
        system.spawn(SubmissionPersistence.behavior(
                       SubmissionId("AA", "2018", 1).toString),
                     actorName)
      submissionPersistence ! ModifySubmission(modified, submissionProbe.ref)
      submissionProbe.expectMessage(SubmissionNotExists(modified.id))
    }

    "return None if it doesn't exist" in {
      val submissionPersistence =
        system.spawn(SubmissionPersistence.behavior(submissionId.toString),
                     actorName)

      submissionPersistence ! GetSubmission(maybeSubmissionProbe.ref)
      maybeSubmissionProbe.expectMessageType[Option[Submission]]

      maybeSubmissionProbe.within(0.seconds, 5.seconds) {
        msg: Option[Submission] =>
          msg match {
            case Some(_) => fail
            case None    => succeed
          }
      }
    }

  }
}
