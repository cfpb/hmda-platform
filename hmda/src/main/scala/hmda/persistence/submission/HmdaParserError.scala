package hmda.persistence.submission

import akka.actor.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ ActorRef, Behavior, TypedActorContext }
import akka.cluster.sharding.typed.ShardingEnvelope
import akka.cluster.sharding.typed.scaladsl.ClusterSharding
import akka.stream.ActorMaterializer
import akka.util.{ ByteString, Timeout }
import hmda.messages.submission.SubmissionProcessingCommands._
import hmda.messages.submission.SubmissionProcessingEvents.{ HmdaRowParsedCount, HmdaRowParsedError, SubmissionProcessingEvent }
import hmda.model.filing.submission.{ Parsed, ParsedWithErrors }
import hmda.persistence.HmdaTypedPersistentActor
import akka.actor.typed.scaladsl.adapter._
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.{ Effect, EventSourcedBehavior, RetentionCriteria }
import akka.persistence.typed.scaladsl.EventSourcedBehavior.CommandHandler
import com.typesafe.config.ConfigFactory
import hmda.model.filing.submissions.PaginatedResource
import hmda.model.processing.state.HmdaParserErrorState
import HmdaProcessingUtils.{ readRawData, updateSubmissionStatus }
import akka.stream.scaladsl.{ Sink, Source }
import akka.stream.typed.scaladsl.ActorFlow
import hmda.parser.filing.ParserFlow._

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.util.{ Failure, Success }
import hmda.query.HmdaQuery._

object HmdaParserError extends HmdaTypedPersistentActor[SubmissionProcessingCommand, SubmissionProcessingEvent, HmdaParserErrorState] {

  override val name: String = "HmdaParserError"

  val config        = ConfigFactory.load()
  val futureTimeout = config.getInt("hmda.actor.timeout")

  implicit val timeout: Timeout = Timeout(futureTimeout.seconds)

  override def behavior(entityId: String): Behavior[SubmissionProcessingCommand] =
    Behaviors.setup { ctx =>
      EventSourcedBehavior[SubmissionProcessingCommand, SubmissionProcessingEvent, HmdaParserErrorState](
        persistenceId = PersistenceId(s"$entityId"),
        emptyState = HmdaParserErrorState(),
        commandHandler = commandHandler(ctx),
        eventHandler = eventHandler
      ).withRetention(RetentionCriteria.snapshotEvery(numberOfEvents = 1000, keepNSnapshots = 10))
    }

  override def commandHandler(
    ctx: TypedActorContext[SubmissionProcessingCommand]
  ): CommandHandler[SubmissionProcessingCommand, SubmissionProcessingEvent, HmdaParserErrorState] = { (state, cmd) =>
    val log                                      = ctx.asScala.log
    implicit val system: ActorSystem             = ctx.asScala.system.toUntyped
    implicit val materializer: ActorMaterializer = ActorMaterializer()
    implicit val ec: ExecutionContext            = system.dispatcher
    val sharding                                 = ClusterSharding(ctx.asScala.system)

    cmd match {
      case StartParsing(submissionId) =>
        log.info(s"Start parsing for ${submissionId.toString}")

        readRawData(submissionId)
          .map(line => line.data)
          .map(ByteString(_))
          .via(parseHmdaFile)
          .zip(Source.fromIterator(() => Iterator.from(1)))
          .collect {
            case ((Left(errors), line), rowNumber) =>
              PersistHmdaRowParsedError(rowNumber, estimateULI(line), errors.map(_.errorMessage), None)
          }
          .via(
            ActorFlow.ask(ctx.asScala.self)(
              (el, replyTo: ActorRef[HmdaRowParsedError]) =>
                PersistHmdaRowParsedError(el.rowNumber, el.estimatedULI, el.errors, Some(replyTo))
            )
          )
          .runWith(Sink.ignore)
          .onComplete {
            case Success(_) =>
              ctx.asScala.self ! CompleteParsing(submissionId)
            case Failure(_) =>
              log.error(s"Uploading failed for $submissionId")
          }

        Effect.none

      case PersistHmdaRowParsedError(rowNumber, estimatedULI, errors, maybeReplyTo) =>
        Effect
          .persist(HmdaRowParsedError(rowNumber, estimatedULI, errors))
          .thenRun { _ =>
            log.debug(s"Persisted error: $rowNumber, $estimatedULI, $errors")
            maybeReplyTo match {
              case Some(replyTo) =>
                replyTo ! HmdaRowParsedError(rowNumber, estimatedULI, errors)
              case None => //do nothing
            }
          }

      case GetParsedWithErrorCount(replyTo) =>
        replyTo ! HmdaRowParsedCount(state.totalErrors)
        Effect.none

      case GetParsingErrors(page, replyTo) =>
        val p = PaginatedResource(state.totalErrors)(page)
        val larErrorsToReturn =
          state.larErrors.slice(p.fromIndex, p.toIndex)
        replyTo ! HmdaParserErrorState(state.transmittalSheetErrors, larErrorsToReturn, state.totalErrors)
        Effect.none

      case CompleteParsing(submissionId) =>
        log.info(s"Completed Parsing for ${submissionId.toString}, total lines with errors: ${state.totalErrors}")
        val updatedStatus = if (state.totalErrors == 0) {
          Parsed
        } else {
          ParsedWithErrors
        }
        updateSubmissionStatus(sharding, submissionId, updatedStatus, log)
        Effect.none

      case HmdaParserStop =>
        Effect.stop()

      case _ =>
        Effect.none
    }
  }

  override def eventHandler: (HmdaParserErrorState, SubmissionProcessingEvent) => HmdaParserErrorState = {
    case (state, error @ HmdaRowParsedError(_, _, _)) =>
      state.update(error)
    case (state, _) => state
  }

  def startShardRegion(sharding: ClusterSharding): ActorRef[ShardingEnvelope[SubmissionProcessingCommand]] =
    super.startShardRegion(sharding)

  def estimateULI(line: String): String = {
    val larItems = line.split('|')
    if (larItems.length >= 3) {
      larItems(2)
    } else {
      "The ULI could not be identified."
    }
  }

}
