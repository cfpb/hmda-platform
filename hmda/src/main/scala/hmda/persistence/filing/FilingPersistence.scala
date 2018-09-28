package hmda.persistence.filing

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import akka.persistence.typed.scaladsl.{Effect, PersistentBehaviors}
import akka.persistence.typed.scaladsl.PersistentBehaviors.CommandHandler
import hmda.messages.filing.FilingCommands._
import hmda.messages.filing.FilingEvents.{FilingEvent, SubmissionAdded}
import hmda.model.filing.submission.Submission

object FilingPersistence {

  final val name = "Filing"

  case class FilingState(submissions: List[Submission] = Nil) {
    def update(event: FilingEvent): FilingState = {
      event match {
        case SubmissionAdded(submission) =>
          if (submissions.contains(submission)) {
            this
          } else {
            FilingState(submission :: submissions)
          }
        case _ => this
      }
    }
  }

  def behavior(lei: String, period: String): Behavior[FilingCommand] =
    Behaviors.setup { ctx =>
      ctx.log.debug(s"Started Filing Persistence: s$lei-$period")
      PersistentBehaviors
        .receive[FilingCommand, FilingEvent, FilingState](
          persistenceId = s"$lei-$period",
          emptyState = FilingState(),
          commandHandler = commandHandler,
          eventHandler = eventHandler
        )
        .snapshotEvery(1000)
        .withTagger(_ => Set(s"$name-$lei-$period"))
    }

  val commandHandler
    : CommandHandler[FilingCommand, FilingEvent, FilingState] = {
    (ctx, state, cmd) =>
      cmd match {
        case AddSubmission(submission, replyTo) =>
          Effect.persist(SubmissionAdded(submission)).thenRun { _ =>
            ctx.log.debug(s"Added submission: ${submission.toString}")
            replyTo ! submission
          }

        case GetLatestSubmission(replyTo) =>
          val maybeSubmission = state.submissions.headOption
          replyTo ! maybeSubmission
          Effect.none

        case GetSubmissions(replyTo) =>
          replyTo ! state.submissions
          Effect.none

        case _ =>
          Effect.none
      }
  }

  val eventHandler: (FilingState, FilingEvent) => FilingState = {
    case (state, evt @ SubmissionAdded(_)) => state.update(evt)
    case (state, _)                        => state
  }

}
