package hmda.persistence.submission

import java.time.Instant

import akka.actor.typed.{ActorContext, ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import akka.cluster.sharding.typed.ShardingEnvelope
import akka.cluster.sharding.typed.scaladsl.ClusterSharding
import akka.persistence.typed.scaladsl.PersistentBehaviors.CommandHandler
import akka.persistence.typed.scaladsl.{Effect, PersistentBehaviors}
import hmda.messages.filing.FilingCommands.AddSubmission
import hmda.messages.submission.SubmissionCommands._
import hmda.messages.submission.SubmissionEvents.{
  SubmissionCreated,
  SubmissionEvent,
  SubmissionModified,
  SubmissionNotExists
}
import hmda.model.filing.submission.{Created, Submission}
import hmda.persistence.HmdaPersistentActor
import hmda.persistence.filing.FilingPersistence

object SubmissionPersistence
    extends HmdaPersistentActor[SubmissionCommand,
                                SubmissionEvent,
                                SubmissionState] {

  final val name = "Submission"

  def behavior(entityId: String): Behavior[SubmissionCommand] =
    Behaviors.setup { ctx =>
      PersistentBehaviors
        .receive[SubmissionCommand, SubmissionEvent, SubmissionState](
          persistenceId = s"$name-$entityId",
          emptyState = SubmissionState(None),
          commandHandler = commandHandler(ctx),
          eventHandler = eventHandler
        )
        .snapshotEvery(1000)
    }

  def commandHandler(ctx: ActorContext[SubmissionCommand])
    : CommandHandler[SubmissionCommand, SubmissionEvent, SubmissionState] = {
    (state, cmd) =>
      val log = ctx.asScala.log
      val sharding = ClusterSharding(ctx.asScala.system)
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
            log.debug(s"persisted new Submission: ${submission.id.toString}")
            val filingPersistence = sharding.entityRefFor(
              FilingPersistence.typeKey,
              s"${FilingPersistence.name}-${submission.id.lei}-${submission.id.period}")
            filingPersistence ! AddSubmission(submission, None)
            replyTo ! SubmissionCreated(submission)
          }
        case ModifySubmission(submission, replyTo) =>
          if (state.submission.map(s => s.id).contains(submission.id)) {
            Effect.persist(SubmissionModified(submission)).thenRun { _ =>
              log.debug(
                s"persisted modified Submission: ${submission.toString}")
              replyTo ! SubmissionModified(submission)
            }
          } else {
            replyTo ! SubmissionNotExists(submission.id)
            Effect.none
          }
        case SubmissionStop() =>
          Effect.stop
      }
  }

  val eventHandler: (SubmissionState, SubmissionEvent) => SubmissionState = {
    case (state, SubmissionCreated(submission)) => state.copy(Some(submission))
    case (state, SubmissionModified(modified)) =>
      if (state.submission.getOrElse(Submission()).id == modified.id) {
        state.copy(Some(modified))
      } else {
        state
      }
    case (state, SubmissionNotExists(_)) => state
  }

  def startShardRegion(sharding: ClusterSharding)
    : ActorRef[ShardingEnvelope[SubmissionCommand]] = {
    super.startShardRegion(sharding, SubmissionStop())
  }

}
