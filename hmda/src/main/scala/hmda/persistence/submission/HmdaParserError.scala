package hmda.persistence.submission

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorContext, ActorRef, Behavior}
import akka.cluster.sharding.typed.ShardingEnvelope
import akka.cluster.sharding.typed.scaladsl.ClusterSharding
import akka.persistence.typed.scaladsl.{Effect, PersistentBehaviors}
import akka.persistence.typed.scaladsl.PersistentBehaviors.CommandHandler
import hmda.messages.submission.SubmissionProcessingCommands.{
  HmdaParserStop,
  StartParsing,
  SubmissionProcessingCommand
}
import hmda.messages.submission.SubmissionProcessingEvents
import hmda.messages.submission.SubmissionProcessingEvents.{
  HmdaRowParsed,
  SubmissionProcessingEvent
}
import hmda.persistence.HmdaTypedPersistentActor

object HmdaParserError
    extends HmdaTypedPersistentActor[SubmissionProcessingCommand,
                                     SubmissionProcessingEvent,
                                     HmdaParserErrorState] {

  override val name: String = "HmdaParserError"

  override def behavior(
      entityId: String): Behavior[SubmissionProcessingCommand] =
    Behaviors.setup { ctx =>
      PersistentBehaviors
        .receive[SubmissionProcessingCommand,
                 SubmissionProcessingEvent,
                 HmdaParserErrorState](
          persistenceId = s"$entityId",
          emptyState = HmdaParserErrorState(),
          commandHandler = commandHandler(ctx),
          eventHandler = eventHandler
        )
        .snapshotEvery(1000)
    }

  override def commandHandler(ctx: ActorContext[SubmissionProcessingCommand])
    : CommandHandler[SubmissionProcessingCommand,
                     SubmissionProcessingEvent,
                     HmdaParserErrorState] = { (state, cmd) =>
    val log = ctx.asScala.log
    cmd match {
      case StartParsing(submissionId) =>
        log.info(s"Start parsing for ${submissionId.toString}")
        Effect.none
      case _ =>
        Effect.none
    }
  }

  override def eventHandler: (
      HmdaParserErrorState,
      SubmissionProcessingEvent) => HmdaParserErrorState = {
    case (state, HmdaRowParsed()) => state
    case (state, _)               => state
  }

  def startShardRegion(sharding: ClusterSharding)
    : ActorRef[ShardingEnvelope[SubmissionProcessingCommand]] = {
    super.startShardRegion(sharding, HmdaParserStop)
  }

}
