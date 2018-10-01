package hmda.projection

import akka.actor.{ActorSystem, Scheduler}
import akka.actor.typed.{ActorContext, Behavior}
import akka.persistence.query.{EventEnvelope, NoOffset, Offset}
import akka.persistence.typed.scaladsl.{Effect, PersistentBehaviors}
import akka.persistence.typed.scaladsl.PersistentBehaviors.CommandHandler
import akka.stream.ActorMaterializer
import hmda.messages.projection.CommonProjectionMessages._
import hmda.query.HmdaQuery._
import akka.actor.typed.scaladsl.AskPattern._
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.scaladsl.adapter._
import akka.stream.scaladsl.Sink
import akka.util.Timeout

import scala.concurrent.Future

trait ResumableProjection {

  val name: String

  implicit val timeout: Timeout

  case class ResumableProjectionState(offset: Offset = NoOffset)

  def behavior: Behavior[ProjectionCommand] =
    Behaviors.setup { ctx =>
      PersistentBehaviors
        .receive[ProjectionCommand, ProjectionEvent, ResumableProjectionState](
          persistenceId = name,
          emptyState = ResumableProjectionState(),
          commandHandler = commandHandler(ctx),
          eventHandler = eventHandler
        )
    }

  def commandHandler(ctx: ActorContext[ProjectionCommand])
    : CommandHandler[ProjectionCommand,
                     ProjectionEvent,
                     ResumableProjectionState] = { (state, cmd) =>
    val log = ctx.asScala.log
    cmd match {
      case StartStreaming =>
        implicit val system: ActorSystem = ctx.asScala.system.toUntyped
        implicit val materializer: ActorMaterializer = ActorMaterializer()
        implicit val scheduler: Scheduler = system.scheduler
        log.info("Streaming messages from {}", name)
        readJournal(system)
          .eventsByTag("institution", state.offset)
          .map { env =>
            log.info(env.toString)
            projectEvent(env)
          }
          .map { env =>
            val actorRef = ctx.asScala.self
            val result: Future[OffsetSaved] = actorRef ? (ref =>
              SaveOffset(env.offset, ref))
            result
          }
          .runWith(Sink.ignore)
        Effect.none

      case SaveOffset(offset, replyTo) =>
        Effect.persist(OffsetSaved(offset)).thenRun { _ =>
          log.info("Offset saved: {}", offset)
          replyTo ! OffsetSaved(offset)
        }

      case GetOffset(replyTo) =>
        replyTo ! OffsetSaved(state.offset)
        Effect.none
    }
  }

  val eventHandler: (ResumableProjectionState,
                     ProjectionEvent) => ResumableProjectionState = {
    case (state, OffsetSaved(offset)) => state.copy(offset = offset)
  }

  def projectEvent(envelope: EventEnvelope): EventEnvelope

}
