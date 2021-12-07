package hmda.persistence.submission

import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}
import akka.cluster.sharding.typed.ShardingEnvelope
import akka.cluster.sharding.typed.scaladsl.{ClusterSharding, EntityRef}
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.EventSourcedBehavior.CommandHandler
import akka.persistence.typed.scaladsl.{Effect, EventSourcedBehavior, RetentionCriteria}
import hmda.messages.submission.HmdaRawDataCommands.{AddLines, HmdaRawDataCommand, StopRawData}
import hmda.messages.submission.HmdaRawDataEvents.{HmdaRawDataEvent, LineAdded}
import hmda.messages.submission.HmdaRawDataReplies.LinesAdded
import hmda.model.filing.submission.SubmissionId
import hmda.model.processing.state.HmdaRawDataState
import hmda.persistence.HmdaTypedPersistentActor

object HmdaRawData extends HmdaTypedPersistentActor[HmdaRawDataCommand, HmdaRawDataEvent, HmdaRawDataState] {

  override val name: String = "HmdaRawData"

  override def behavior(entityId: String): Behavior[HmdaRawDataCommand] =
    Behaviors.setup { ctx =>
      EventSourcedBehavior[HmdaRawDataCommand, HmdaRawDataEvent, HmdaRawDataState](
        persistenceId = PersistenceId.ofUniqueId(entityId),
        emptyState = HmdaRawDataState(),
        commandHandler = commandHandler(ctx),
        eventHandler = eventHandler
      ).withRetention(RetentionCriteria.snapshotEvery(numberOfEvents = 1000, keepNSnapshots = 10))
    }

  override def commandHandler(
                               ctx: ActorContext[HmdaRawDataCommand]
                             ): CommandHandler[HmdaRawDataCommand, HmdaRawDataEvent, HmdaRawDataState] = { (_, cmd) =>
    val log = ctx.log
    cmd match {
      case AddLines(_, timestamp, data, maybeReplyTo) =>
        val evts = data.map(LineAdded(timestamp, _)).toList
        Effect.persist(evts).thenRun { _ =>
          log.debug(s"Persisted: $data")
          maybeReplyTo match {
            case Some(replyTo) => replyTo ! LinesAdded(evts)
            case None => //Do Nothing
          }
        }

      case StopRawData =>
        Effect.stop()
    }
  }

  override def eventHandler: (HmdaRawDataState, HmdaRawDataEvent) => HmdaRawDataState = {
    case (state, evt @ LineAdded(_, _)) => state.update(evt)
  }

  def startShardRegion(sharding: ClusterSharding): ActorRef[ShardingEnvelope[HmdaRawDataCommand]] =
    super.startShardRegion(sharding, StopRawData)

  def selectHmdaRawData(sharding: ClusterSharding, submissionId: SubmissionId): EntityRef[HmdaRawDataCommand] =
    sharding.entityRefFor(HmdaRawData.typeKey, s"${HmdaRawData.name}-${submissionId.toString}")

}