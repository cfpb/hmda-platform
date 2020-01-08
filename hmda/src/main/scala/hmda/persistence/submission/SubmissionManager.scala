package hmda.persistence.submission

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import akka.cluster.sharding.typed.ShardingEnvelope
import akka.cluster.sharding.typed.scaladsl.{ClusterSharding, EntityRef}
import hmda.actor.HmdaTypedActor
import hmda.messages.submission.SubmissionCommands.ModifySubmission
import hmda.messages.submission.SubmissionEvents.{SubmissionEvent, SubmissionModified}
import hmda.messages.submission.SubmissionManagerCommands._
import hmda.messages.submission.SubmissionProcessingCommands.{StartMacro, StartParsing, StartQuality, StartSyntacticalValidity}
import hmda.model.filing.submission._

object SubmissionManager extends HmdaTypedActor[SubmissionManagerCommand] {

  override val name: String = "SubmissionManager"

  override def behavior(entityId: String): Behavior[SubmissionManagerCommand] =
    Behaviors.setup { ctx =>
      val log = ctx.log
      log.info(s"Started $entityId")

      val sharding = ClusterSharding(ctx.system)

      val submissionId = entityId.replaceAll(s"$name-", "")

      val submissionPersistence =
        sharding.entityRefFor(SubmissionPersistence.typeKey, s"${SubmissionPersistence.name}-$submissionId")

      val hmdaParserError =
        sharding.entityRefFor(HmdaParserError.typeKey, s"${HmdaParserError.name}-$submissionId")

      val hmdaValidationError =
        sharding.entityRefFor(HmdaValidationError.typeKey, s"${HmdaValidationError.name}-$submissionId")

      val submissionEventResponseAdapter: ActorRef[SubmissionEvent] =
        ctx.messageAdapter(response => WrappedSubmissionEventResponse(response))

      Behaviors.receiveMessage {
        case UpdateSubmissionStatus(modified) =>
          submissionPersistence ! ModifySubmission(modified, submissionEventResponseAdapter)
          Behaviors.same

        case WrappedSubmissionEventResponse(submissionEvent) =>
          submissionEvent match {
            case SubmissionModified(submission) =>
              submission.status match {
                case Uploaded =>
                  hmdaParserError ! StartParsing(submission.id)
                case Parsed =>
                  hmdaValidationError ! StartSyntacticalValidity(submission.id)
                case SyntacticalOrValidity =>
                  hmdaValidationError ! StartQuality(submission.id)
                case Quality | QualityErrors =>
                  hmdaValidationError ! StartMacro(submission.id)

                case _ =>
              }
              Behaviors.same
            case _ =>
              log.info(s"$submissionEvent")
              Behaviors.same
          }
        case SubmissionManagerStop =>
          log.info(s"Stopping ${ctx.asScala.self.path.name}")
          Behaviors.stopped
      }
    }

  def startShardRegion(sharding: ClusterSharding): ActorRef[ShardingEnvelope[SubmissionManagerCommand]] =
    super.startShardRegion(sharding, SubmissionManagerStop)

  def selectSubmissionManager(sharding: ClusterSharding, submissionId: SubmissionId): EntityRef[SubmissionManagerCommand] =
    sharding.entityRefFor(SubmissionManager.typeKey, s"${SubmissionManager.name}-${submissionId.toString}")

}