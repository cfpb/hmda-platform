package hmda.persistence.submission

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorContext, ActorRef, Behavior}
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.{Effect, PersistentBehavior}
import akka.persistence.typed.scaladsl.PersistentBehavior.CommandHandler
import hmda.persistence.HmdaTypedPersistentActor
import akka.cluster.sharding.typed.ShardingEnvelope
import akka.cluster.sharding.typed.scaladsl.ClusterSharding
import hmda.messages.submission.EditDetailPersistenceCommands.{
  EditDetailsPersistenceCommand,
  GetEditRowCount,
  PersistEditDetails
}
import hmda.messages.submission.EditDetailPersistenceEvents.{
  EditDetailsAdded,
  EditDetailsPersistenceEvent,
  EditDetailsRowCounted
}

object EditDetailsPersistence
    extends HmdaTypedPersistentActor[EditDetailsPersistenceCommand,
                                     EditDetailsPersistenceEvent,
                                     EditDetailsPersistenceState] {

  override val name: String = "EditDetail"

  override def behavior(
      entityId: String): Behavior[EditDetailsPersistenceCommand] = {
    Behaviors.setup { ctx =>
      PersistentBehavior(
        persistenceId = PersistenceId(entityId),
        emptyState = EditDetailsPersistenceState(),
        commandHandler = commandHandler(ctx),
        eventHandler = eventHandler
      )
    }
  }

  override def commandHandler(ctx: ActorContext[EditDetailsPersistenceCommand])
    : CommandHandler[EditDetailsPersistenceCommand,
                     EditDetailsPersistenceEvent,
                     EditDetailsPersistenceState] = { (state, cmd) =>
    val log = ctx.asScala.log
    cmd match {
      case PersistEditDetails(editDetail, maybeReplyTo) =>
        val evt = EditDetailsAdded(editDetail)
        Effect.persist(evt).thenRun { _ =>
          log.info(s"Persisted: $evt")
          maybeReplyTo match {
            case Some(replyTo) =>
              replyTo ! evt
            case None => //Do nothing
          }
        }

      case GetEditRowCount(editName, replyTo) =>
        replyTo ! EditDetailsRowCounted(
          state.totalErrorMap.getOrElse(editName, 0))
        Effect.none
    }
  }

  override def eventHandler
    : (EditDetailsPersistenceState,
       EditDetailsPersistenceEvent) => EditDetailsPersistenceState = {
    case (state, evt @ EditDetailsAdded(_)) => state.update(evt)
    case (state, _)                         => state
  }

  def startShardRegion(sharding: ClusterSharding)
    : ActorRef[ShardingEnvelope[EditDetailsPersistenceCommand]] = {
    super.startShardRegion(sharding)
  }

}
