package hmda.persistence.filing

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.cluster.sharding.typed.ShardingEnvelope
import akka.cluster.sharding.typed.scaladsl.{ClusterSharding, EntityTypeKey}
import akka.persistence.typed.scaladsl.{Effect, PersistentBehaviors}
import akka.persistence.typed.scaladsl.PersistentBehaviors.CommandHandler
import hmda.messages.filing.FilingCommands._
import hmda.messages.filing.FilingEvents.{
  FilingCreated,
  FilingEvent,
  SubmissionAdded
}
import hmda.model.filing.FilingDetails
import hmda.persistence.HmdaPersistentActor

object FilingPersistence
    extends HmdaPersistentActor[FilingCommand, FilingEvent, FilingState] {

  final val name = "Filing"

  val ShardingTypeName = EntityTypeKey[FilingCommand](name)

  def behavior(filingId: String): Behavior[FilingCommand] =
    Behaviors.setup { ctx =>
      ctx.log.debug(s"Started Filing Persistence: s$filingId")
      PersistentBehaviors
        .receive[FilingCommand, FilingEvent, FilingState](
          persistenceId = s"$name-$filingId",
          emptyState = FilingState(),
          commandHandler = commandHandler(ctx),
          eventHandler = eventHandler
        )
        .snapshotEvery(1000)
        .withTagger(_ => Set(name.toLowerCase()))
    }

  def commandHandler(ctx: ActorContext[FilingCommand])
    : CommandHandler[FilingCommand, FilingEvent, FilingState] = {
    (state, cmd) =>
      val log = ctx.asScala.log
      cmd match {
        case CreateFiling(filing, replyTo) =>
          Effect.persist(FilingCreated(filing)).thenRun { _ =>
            log.debug(s"Filing created: ${filing.lei}-${filing.period}")
            replyTo ! FilingCreated(filing)
          }

        case GetFiling(replyTo) =>
          if (state.filing.isEmpty) {
            replyTo ! None
          } else {
            replyTo ! Some(state.filing)
          }
          Effect.none

        case GetFilingDetails(replyTo) =>
          replyTo ! FilingDetails(state.filing, state.submissions)
          Effect.none

        case AddSubmission(submission, replyTo) =>
          Effect.persist(SubmissionAdded(submission)).thenRun { _ =>
            log.debug(s"Added submission: ${submission.toString}")
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
    case (state, evt @ FilingCreated(_))   => state.update(evt)
    case (state, _)                        => state
  }

  def startShardRegion(
      sharding: ClusterSharding): ActorRef[ShardingEnvelope[FilingCommand]] = {
    super.startShardRegion(sharding, FilingStop)
  }

}
