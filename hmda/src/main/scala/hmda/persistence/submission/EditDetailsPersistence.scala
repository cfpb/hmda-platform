package hmda.persistence.submission

import akka.actor.typed.scaladsl.{ ActorContext, Behaviors }
import akka.actor.typed.{ ActorRef, Behavior }
import akka.cluster.sharding.typed.ShardingEnvelope
import akka.cluster.sharding.typed.scaladsl.{ ClusterSharding, EntityRef }
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.EventSourcedBehavior.CommandHandler
import akka.persistence.typed.scaladsl.{ Effect, EventSourcedBehavior }
import hmda.messages.submission.EditDetailsCommands.{ EditDetailsPersistenceCommand, GetEditRowCount, PersistEditDetails, StopEditDetails }
import hmda.messages.submission.EditDetailsEvents.{ EditDetailsAdded, EditDetailsPersistenceEvent, EditDetailsRowCounted }
import hmda.model.filing.submission.SubmissionId
import hmda.persistence.HmdaTypedPersistentActor

object EditDetailsPersistence
  extends HmdaTypedPersistentActor[EditDetailsPersistenceCommand, EditDetailsPersistenceEvent, EditDetailsPersistenceState] {

  override val name: String = "EditDetail"

  override def behavior(entityId: String): Behavior[EditDetailsPersistenceCommand] =
    Behaviors.setup { ctx =>
      EventSourcedBehavior(
        persistenceId = PersistenceId.ofUniqueId(entityId),
        emptyState = EditDetailsPersistenceState(),
        commandHandler = commandHandler(ctx),
        eventHandler = eventHandler
      )
    }

  override def commandHandler(
                               ctx: ActorContext[EditDetailsPersistenceCommand]
                             ): CommandHandler[EditDetailsPersistenceCommand, EditDetailsPersistenceEvent, EditDetailsPersistenceState] = { (state, cmd) =>
    val log = ctx.log
    cmd match {
      case PersistEditDetails(editDetail, maybeReplyTo) =>
        val evt = EditDetailsAdded(editDetail)
        Effect.persist(evt).thenRun { _ =>
          log.debug(s"Persisted: $evt")
          maybeReplyTo match {
            case Some(replyTo) =>
              replyTo ! evt
            case None => //Do nothing
          }
        }

      case GetEditRowCount(editName, replyTo) =>
        replyTo ! EditDetailsRowCounted(state.totalErrorMap.getOrElse(editName, 0))
        Effect.none

      case StopEditDetails =>
        log.info(s"Stopping ${ctx.self.path.name}")
        Effect.stop()
    }
  }

  override def eventHandler: (EditDetailsPersistenceState, EditDetailsPersistenceEvent) => EditDetailsPersistenceState = {
    case (state, evt @ EditDetailsAdded(_)) => state.update(evt)
    case (state, _)                         => state
  }

  def startShardRegion(sharding: ClusterSharding): ActorRef[ShardingEnvelope[EditDetailsPersistenceCommand]] =
    super.startShardRegion(sharding, StopEditDetails)

  def selectEditDetailsPersistence(sharding: ClusterSharding, submissionId: SubmissionId): EntityRef[EditDetailsPersistenceCommand] =
    sharding.entityRefFor(EditDetailsPersistence.typeKey, s"${EditDetailsPersistence.name}-$submissionId")

}