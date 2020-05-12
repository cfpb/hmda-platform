package hmda.projection

import akka.actor.typed.scaladsl.AskPattern._
import akka.actor.typed.scaladsl.{ ActorContext, Behaviors }
import akka.actor.typed.{ ActorSystem, Behavior, Scheduler }
import akka.persistence.query.{ EventEnvelope, NoOffset, Offset }
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.EventSourcedBehavior.CommandHandler
import akka.persistence.typed.scaladsl.{ Effect, EventSourcedBehavior }
import akka.stream.Materializer
import akka.stream.scaladsl.Sink
import akka.util.Timeout
import hmda.messages.projection.CommonProjectionMessages._
import hmda.query.HmdaQuery._

import scala.concurrent.Future
// This is just a Guardian for starting up the API
// $COVERAGE-OFF$

trait ResumableProjection {

  val name: String

  implicit val timeout: Timeout

  case class ResumableProjectionState(offset: Offset = NoOffset)

  def behavior: Behavior[ProjectionCommand] =
    Behaviors.setup { ctx =>
      EventSourcedBehavior[ProjectionCommand, ProjectionEvent, ResumableProjectionState](
        persistenceId = PersistenceId.ofUniqueId(name),
        emptyState = ResumableProjectionState(),
        commandHandler = commandHandler(ctx),
        eventHandler = eventHandler
      )
    }

  def commandHandler(
                      ctx: ActorContext[ProjectionCommand]
                    ): CommandHandler[ProjectionCommand, ProjectionEvent, ResumableProjectionState] = { (state, cmd) =>
    val log = ctx.log
    cmd match {
      case StartStreaming =>
        val system: ActorSystem[_]              = ctx.system
        implicit val materializer: Materializer = Materializer(ctx)
        implicit val scheduler: Scheduler       = system.scheduler
        log.info("Streaming messages from {}", name)
        readJournal(system)
          .eventsByTag("institution", state.offset)
          .map { env =>
            log.info(env.toString)
            projectEvent(env)
          }
          .map { env =>
            val actorRef                    = ctx.asScala.self
            val result: Future[OffsetSaved] = actorRef ? (ref => SaveOffset(env.offset, ref))
            result
          }
          .runWith(Sink.onComplete(_ => log.error("The Institutions API has stopped streaming")))
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

  val eventHandler: (ResumableProjectionState, ProjectionEvent) => ResumableProjectionState = {
    case (state, OffsetSaved(offset)) => state.copy(offset = offset)
  }

  def projectEvent(envelope: EventEnvelope): EventEnvelope

}
// This is just a Guardian for starting up the API
// $COVERAGE-OFF$
