package hmda.persistence.submission

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorContext, ActorRef, Behavior}
import akka.cluster.sharding.typed.ShardingEnvelope
import akka.cluster.sharding.typed.scaladsl.ClusterSharding
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.{Effect, PersistentBehavior}
import akka.persistence.typed.scaladsl.PersistentBehavior.CommandHandler
import hmda.messages.submission.HmdaRawDataCommands.{
  AddLine,
  HmdaRawDataCommand
}
import hmda.messages.submission.HmdaRawDataEvents.{HmdaRawDataEvent, LineAdded}
import hmda.persistence.HmdaTypedPersistentActor

case class HmdaRawDataState(size: Int = 0) {
  def update(event: HmdaRawDataEvent): HmdaRawDataState = event match {
    case LineAdded(_, _) =>
      HmdaRawDataState(size + 1)
    case _ => this
  }
}

object HmdaRawData
    extends HmdaTypedPersistentActor[HmdaRawDataCommand,
                                     HmdaRawDataEvent,
                                     HmdaRawDataState] {

  override val name: String = "HmdaRawData"

  override def behavior(entityId: String): Behavior[HmdaRawDataCommand] =
    Behaviors.setup { ctx =>
      PersistentBehavior[HmdaRawDataCommand,
                         HmdaRawDataEvent,
                         HmdaRawDataState](
        persistenceId = PersistenceId(entityId),
        emptyState = HmdaRawDataState(),
        commandHandler = commandHandler(ctx),
        eventHandler = eventHandler
      ).snapshotEvery(1000)
    }

  override def commandHandler(ctx: ActorContext[HmdaRawDataCommand])
    : CommandHandler[HmdaRawDataCommand, HmdaRawDataEvent, HmdaRawDataState] = {
    (_, cmd) =>
      val log = ctx.asScala.log
      cmd match {
        case AddLine(_, timestamp, data, maybeReplyTo) =>
          val evt = LineAdded(timestamp, data)
          Effect.persist(evt).thenRun { _ =>
            log.debug(s"Persisted: $data")
            maybeReplyTo match {
              case Some(replyTo) =>
                replyTo ! evt
              case None => //Do Nothing
            }
          }
      }
  }

  override def eventHandler
    : (HmdaRawDataState, HmdaRawDataEvent) => HmdaRawDataState = {
    case (state, evt @ LineAdded(_, _)) => state.update(evt)
  }

  def startShardRegion(sharding: ClusterSharding)
    : ActorRef[ShardingEnvelope[HmdaRawDataCommand]] = {
    super.startShardRegion(sharding)
  }

}
