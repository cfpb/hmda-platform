package hmda.persistence.submission

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import akka.cluster.sharding.typed.ShardingEnvelope
import akka.cluster.sharding.typed.scaladsl.ClusterSharding
import hmda.actor.HmdaTypedActor
import hmda.messages.submission.SubmissionCommands.ModifySubmission
import hmda.messages.submission.SubmissionEvents.{
  SubmissionEvent,
  SubmissionModified
}
import hmda.messages.submission.SubmissionManagerCommands._
import hmda.model.filing.submission.Uploaded

object SubmissionManager extends HmdaTypedActor[SubmissionManagerCommand] {

  override val name: String = "SubmissionManager"

  override def behavior(entityId: String): Behavior[SubmissionManagerCommand] =
    Behaviors.setup { ctx =>
      val log = ctx.log
      log.info(s"Started $entityId")

      val sharding = ClusterSharding(ctx.system)

      val submissionId = entityId.replaceAll(s"$name-", "")

      val submissionPersistence =
        sharding.entityRefFor(SubmissionPersistence.typeKey,
                              s"${SubmissionPersistence.name}-$submissionId")

      val submissionEventResponseAdapter: ActorRef[SubmissionEvent] =
        ctx.messageAdapter(response => WrappedSubmissionEventResponse(response))

      Behaviors.receiveMessage {
        case UpdateSubmissionStatus(modified) =>
          submissionPersistence ! ModifySubmission(
            modified,
            submissionEventResponseAdapter)
          Behaviors.same

        case WrappedSubmissionEventResponse(submissionEvent) =>
          submissionEvent match {
            case SubmissionModified(submission) =>
              submission.status match {
                case Uploaded =>
                  log.info(s"Data uploaded for ${submission.id.toString}")
                case _ =>
              }
              Behaviors.same
            case _ =>
              log.info(s"$submissionEvent")
              Behaviors.same
          }
      }

    }

  def startShardRegion(sharding: ClusterSharding)
    : ActorRef[ShardingEnvelope[SubmissionManagerCommand]] = {
    super.startShardRegion(sharding, SubmissionManagerStop)
  }
}
