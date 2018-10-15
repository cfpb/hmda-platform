package hmda.persistence.submission

import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}
import akka.cluster.sharding.typed.ShardingEnvelope
import akka.cluster.sharding.typed.scaladsl.ClusterSharding
import akka.persistence.typed.scaladsl.{Effect, PersistentBehaviors}
import akka.persistence.typed.scaladsl.PersistentBehaviors.CommandHandler
import hmda.messages.submission.SubmissionManagerCommands._
import hmda.model.filing.submission._
import hmda.persistence.HmdaPersistentActor

object SubmissionManager
    extends HmdaPersistentActor[SubmissionManagerCommand,
                                SubmissionStatus,
                                SubmissionManagerState] {
  override val name: String = "SubmissionManager"

  override def behavior(entityId: String): Behavior[SubmissionManagerCommand] =
    Behaviors.setup { ctx =>
      PersistentBehaviors
        .receive[SubmissionManagerCommand,
                 SubmissionStatus,
                 SubmissionManagerState](
          persistenceId = s"$name-$entityId",
          emptyState = SubmissionManagerState(Created),
          commandHandler = commandHandler(ctx),
          eventHandler = eventHandler
        )
    }

  def commandHandler(ctx: ActorContext[SubmissionManagerCommand])
    : CommandHandler[SubmissionManagerCommand,
                     SubmissionStatus,
                     SubmissionManagerState] = { (state, cmd) =>
    val log = ctx.asScala.log
    val sharding = ClusterSharding(ctx.asScala.system)
    cmd match {
      case Create(submissionId) =>
        log.debug(s"Created: ${submissionId.toString}")
        Effect.persist(Created)
      case StartUpload(submissionId) =>
        log.debug(s"Start Upload: ${submissionId.toString}")
        Effect.persist(Uploading)
      case CompleteUpload(submissiondId) =>
        log.debug(s"Uploaded: ${submissiondId.toString}")
        Effect.persist(Uploaded)
      case StartParsing(submissionId) =>
        log.debug(s"Parsing: ${submissionId.toString}")
        Effect.persist(Parsing)
      case CompleteParsing(submissionId) =>
        log.debug(s"Parsed: ${submissionId.toString}")
        Effect.persist(Parsed)
      case CompleteParsingWithErrors(submissionId) =>
        log.debug(s"Parsed with Errors: ${submissionId.toString}")
        Effect.persist(ParsedWithErrors)
      case StartSyntacticalValidity(submissionId) =>
        log.debug(
          s"Start Validating Syntactical and Validity: ${submissionId.toString}")
        Effect.persist(Validating)
      case CompleteSyntacticalValidity(submissionId) =>
        log.debug(
          s"Validated Syntactical and Validity: ${submissionId.toString}")
        Effect.none
      case CompleteSyntacticalValidityWithErrors(submissionId) =>
        log.debug(
          s"Validated with Syntactical or Validity Errors: ${submissionId.toString}")
        Effect.persist(SyntacticalOrValidityErrors)
      case StartQuality(submissionId) =>
        log.debug(s"Start Quality: ${submissionId.toString}")
        Effect.none
      case CompleteQuality(submissionId) =>
        log.debug(s"Validated Quality: ${submissionId.toString}")
        Effect.none
      case CompleteQualityWithErrors(submissionId) =>
        log.debug(s"Validated with Quality Errors: ${submissionId.toString}")
        Effect.persist(QualityErrors)
      case StartMacro(submissionId) =>
        log.debug(s"Start Macro: ${submissionId.toString}")
        Effect.none
      case CompleteMacro(submissionId) =>
        log.debug(s"Validated Macro: ${submissionId.toString}")
        Effect.persist(Validated)
      case CompleteMacroWithErrors(submissionId) =>
        log.debug(s"Validated with Macro Errors: ${submissionId.toString}")
        Effect.persist(MacroErrors)
      case Sign(submissionId) =>
        log.debug(s"Sign: ${submissionId.toString}")
        Effect.persist(Signed)
      case Fail(submissionId) =>
        log.error(s"Failed: ${submissionId.toString}")
        Effect.persist(Failed)
      case GetSubmissionStatus(replyTo) =>
        replyTo ! state.submissionStatus
        Effect.none
      case SubmissionManagerStop() =>
        Effect.stop

    }
  }

  override val eventHandler
    : (SubmissionManagerState, SubmissionStatus) => SubmissionManagerState = {
    case (_, submissionStatus) => SubmissionManagerState(submissionStatus)
  }

  def startShardRegion(sharding: ClusterSharding)
    : ActorRef[ShardingEnvelope[SubmissionManagerCommand]] = {
    super.startShardRegion(sharding, SubmissionManagerStop())
  }
}
