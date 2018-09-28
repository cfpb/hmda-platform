package hmda.persistence.submission

import java.time.Instant

import akka.actor.typed.Behavior
import akka.persistence.typed.scaladsl.PersistentBehaviors.CommandHandler
import akka.persistence.typed.scaladsl.{
  Effect,
  PersistentBehavior,
  PersistentBehaviors
}
import hmda.messages.submission.SubmissionCommands.{
  CreateSubmission,
  GetSubmission,
  ModifySubmission,
  SubmissionCommand
}
import hmda.messages.submission.SubmissionEvents.{
  SubmissionCreated,
  SubmissionEvent,
  SubmissionModified,
  SubmissionNotExists
}
import hmda.model.filing.submission.{Created, Submission, SubmissionId}

object SubmissionPersistence {

  case class SubmissionState(submission: Option[Submission])

  final val name = "Submission"

  def behavior(submissionId: SubmissionId): Behavior[SubmissionCommand] =
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
          if (state.submission.map(s => s.id).contains(submission.id)) {
            Effect.persist(SubmissionModified(submission)).thenRun { _ =>
              ctx.log.debug(
                s"persisted modified Submission: ${submission.toString}")
              replyTo ! SubmissionModified(submission)
            }
          } else {
            replyTo ! SubmissionNotExists(submission.id)
            Effect.none
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
