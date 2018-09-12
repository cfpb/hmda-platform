package hmda.persistence.processing

import java.time.Instant

import akka.actor.typed.{ActorRef, Behavior}
import akka.persistence.typed.scaladsl.PersistentBehaviors.CommandHandler
import akka.persistence.typed.scaladsl.{Effect, PersistentBehaviors}
import hmda.messages.CommonMessages.{Command, Event}
import hmda.model.filing.submission.{Created, Submission, SubmissionId}
import hmda.persistence.serialization.submission.SubmissionIdMessage

object SubmissionPersistence {

  sealed trait SubmissionEvent extends Event
  sealed trait SubmissionCommand extends Command

  case class GetSubmission(replyTo: ActorRef[Option[Submission]])
      extends SubmissionCommand
  case class CreateSubmission(submissionId: SubmissionId,
                              replyTo: ActorRef[SubmissionEvent])
      extends SubmissionCommand
  case class ModifySubmission(submission: Submission,
                              replyTo: ActorRef[SubmissionEvent])
      extends SubmissionCommand

  case class EmptySubmission(submission: Submission = Submission())
      extends SubmissionEvent
  case class SubmissionCreated(submission: Submission) extends SubmissionEvent
  case class SubmissionModified(submission: Submission) extends SubmissionEvent

  case class SubmissionState(submission: Option[Submission])

  final val name = "Submission"

  def behavior(submissionId: SubmissionId) =
    PersistentBehaviors
      .receive[SubmissionCommand, SubmissionEvent, SubmissionState](
        persistenceId = submissionId.toString,
        emptyState = SubmissionState(None),
        commandHandler = commandHandler,
        eventHandler = eventHandler
      )
      .snapshotEvery(1000)
      .withTagger(_ => Set(s"$name-${submissionId.lei}"))

  val commandHandler
    : CommandHandler[SubmissionCommand, SubmissionEvent, SubmissionState] = {
    (ctx, state, cmd) =>
      cmd match {
        case GetSubmission(replyTo) =>
          replyTo ! state.submission
          Effect.none
        case CreateSubmission(submissionId, replyTo) =>
          val submission = Submission(
            submissionId,
            Created,
            Instant.now().toEpochMilli
          )
          Effect.persist(SubmissionCreated(submission)).thenRun { _ =>
            ctx.log.debug(
              s"persisted new Submission: ${submission.id.toString}")
            replyTo ! SubmissionCreated(submission)
          }
        case ModifySubmission(submission, replyTo) =>
          Effect.persist(SubmissionModified(submission)).thenRun { _ =>
            ctx.log.debug(
              s"persisted modified Submission: ${submission.toString}")
            replyTo ! SubmissionModified(submission)
          }

      }
  }

  val eventHandler: (SubmissionState, SubmissionEvent) => SubmissionState = {
    case (state, SubmissionCreated(submission)) => state.copy(Some(submission))
    case (state, SubmissionModified(submission)) =>
      if (state.submission.getOrElse(Submission()).id == submission.id) {
        SubmissionState(Some(submission))
      } else {
        state
      }
  }

}
