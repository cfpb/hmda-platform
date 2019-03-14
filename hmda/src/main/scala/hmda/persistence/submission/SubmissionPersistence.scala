package hmda.persistence.submission

import java.time.Instant

import akka.actor.typed.{ActorContext, ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import akka.cluster.sharding.typed.ShardingEnvelope
import akka.cluster.sharding.typed.scaladsl.ClusterSharding
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.{Effect, PersistentBehavior}
import akka.persistence.typed.scaladsl.PersistentBehavior.CommandHandler
import hmda.messages.filing.FilingCommands.{AddSubmission, UpdateSubmission}
import hmda.messages.submission.SubmissionCommands._
import hmda.messages.submission.SubmissionEvents.{
  SubmissionCreated,
  SubmissionEvent,
  SubmissionModified,
  SubmissionNotExists
}
import hmda.model.filing.submission.{
  Created,
  Signed,
  Submission,
  SubmissionStatus
}
import hmda.persistence.HmdaTypedPersistentActor
import hmda.persistence.filing.FilingPersistence

object SubmissionPersistence
    extends HmdaTypedPersistentActor[SubmissionCommand,
                                     SubmissionEvent,
                                     SubmissionState] {

  override final val name = "Submission"

  override def behavior(entityId: String): Behavior[SubmissionCommand] =
    Behaviors.setup { ctx =>
      PersistentBehavior[SubmissionCommand, SubmissionEvent, SubmissionState](
        persistenceId = PersistenceId(s"$entityId"),
        emptyState = SubmissionState(None),
        commandHandler = commandHandler(ctx),
        eventHandler = eventHandler
      ).snapshotEvery(1000)
    }

  override def commandHandler(ctx: ActorContext[SubmissionCommand])
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
        case ModifySubmission(modified, replyTo) =>
          if (state.submission.map(s => s.id).contains(modified.id)) {
            if (modified.status == SubmissionStatus.valueOf(Signed.code) && (modified.end == 0 || modified.receipt.isEmpty)) {
              //for when submission is signed but end date and receipt are empty
              val timestamp = Instant.now().toEpochMilli
              val modifiedSigned = modified.copy(end = timestamp,
                                                 receipt =
                                                   s"${modified.id}-$timestamp")
              Effect.persist(SubmissionModified(modifiedSigned)).thenRun { _ =>
                log.debug(
                  s"persisted modified Submission: ${modifiedSigned.toString}")
                val filingPersistence = sharding.entityRefFor(
                  FilingPersistence.typeKey,
                  s"${FilingPersistence.name}-${modified.id.lei}-${modifiedSigned.id.period}")
                filingPersistence ! UpdateSubmission(modifiedSigned, None)
                replyTo ! SubmissionModified(modifiedSigned)
              }
            } else if (modified.end == 0 && modified.receipt.isEmpty) {
              //for all statuses other than signed
              Effect.persist(SubmissionModified(modified)).thenRun { _ =>
                log.debug(
                  s"persisted modified Submission: ${modified.toString}")
                val filingPersistence = sharding.entityRefFor(
                  FilingPersistence.typeKey,
                  s"${FilingPersistence.name}-${modified.id.lei}-${modified.id.period}")
                filingPersistence ! UpdateSubmission(modified, None)
                replyTo ! SubmissionModified(modified)
              }
            } else {
              //if entered here with end date not 0 and receipt not empty and status other than signed, then don't do anything.
              //Since enddate is not 0 and receipt is not empty, the submission has already been signed
              log.debug(s"No effect: ${modified.toString}")
              Effect.none
            }
          } else {
            replyTo ! SubmissionNotExists(modified.id)
            Effect.none
          }
        case SubmissionStop() =>
          Effect.stop()
      }
  }

  override val eventHandler
    : (SubmissionState, SubmissionEvent) => SubmissionState = {
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
    super.startShardRegion(sharding)
  }

}
