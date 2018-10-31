package hmda.persistence.filing

import akka.actor.typed.{ActorContext, ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import akka.cluster.sharding.typed.ShardingEnvelope
import akka.cluster.sharding.typed.scaladsl.{ClusterSharding, EntityTypeKey}
import akka.persistence.typed.scaladsl.{Effect, PersistentBehaviors}
import akka.persistence.typed.scaladsl.PersistentBehaviors.CommandHandler
import hmda.messages.filing.FilingCommands._
import hmda.messages.filing.FilingEvents.{
  FilingCreated,
  FilingEvent,
  SubmissionAdded,
  SubmissionUpdated
}
import hmda.model.filing.FilingDetails
import hmda.persistence.HmdaTypedPersistentActor

object FilingPersistence
    extends HmdaTypedPersistentActor[FilingCommand, FilingEvent, FilingState] {

  override final val name = "Filing"

  val ShardingTypeName = EntityTypeKey[FilingCommand](name)

  override def behavior(filingId: String): Behavior[FilingCommand] =
    Behaviors.setup { ctx =>
      ctx.log.debug(s"Started Filing Persistence: s$filingId")
      PersistentBehaviors
        .receive[FilingCommand, FilingEvent, FilingState](
          persistenceId = s"$filingId",
          emptyState = FilingState(),
          commandHandler = commandHandler(ctx),
          eventHandler = eventHandler
        )
        .snapshotEvery(1000)
        .withTagger(_ => Set(name.toLowerCase()))
    }

  override def commandHandler(ctx: ActorContext[FilingCommand])
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
          if (state.filing.isEmpty) {
            replyTo ! None
          } else {
            replyTo ! Some(FilingDetails(state.filing, state.submissions))
          }
          Effect.none

        case AddSubmission(submission, replyTo) =>
          Effect.persist(SubmissionAdded(submission)).thenRun { _ =>
            log.debug(s"Added submission: ${submission.toString}")
            replyTo match {
              case Some(ref) => ref ! submission
              case None      => Effect.none //Do not reply
            }
          }

        case UpdateSubmission(updated, replyTo) =>
          if (state.submissions.map(_.id).contains(updated.id)) {
            Effect.persist(SubmissionUpdated(updated)).thenRun { _ =>
              log.debug(s"Updated submission: ${updated.toString}")
              replyTo match {
                case Some(ref) => ref ! updated
                case None      => Effect.none //Do not reply
              }
            }
          } else {
            log.warning(s"Could not update submission wth $updated")
            Effect.none
          }

        case GetLatestSubmission(replyTo) =>
          val maybeSubmission = state.submissions
            .sortWith(_.id.sequenceNumber > _.id.sequenceNumber)
            .headOption
          replyTo ! maybeSubmission
          Effect.none

        case GetSubmissions(replyTo) =>
          replyTo ! state.submissions
          Effect.none

        case FilingStop() =>
          Effect.stop

        case _ =>
          Effect.unhandled
      }
  }

  val eventHandler: (FilingState, FilingEvent) => FilingState = {
    case (state, evt @ SubmissionAdded(_))   => state.update(evt)
    case (state, evt @ FilingCreated(_))     => state.update(evt)
    case (state, evt @ SubmissionUpdated(_)) => state.update(evt)
    case (state, _)                          => state
  }

  def startShardRegion(
      sharding: ClusterSharding): ActorRef[ShardingEnvelope[FilingCommand]] = {
    super.startShardRegion(sharding, FilingStop())
  }

}
