package hmda.institution.projection

import akka.actor.Scheduler
import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.AskPattern._
import hmda.query.HmdaQuery._
import akka.actor.typed.scaladsl.adapter._
import akka.persistence.query.Sequence
import akka.persistence.typed.scaladsl.{Effect, PersistentBehaviors}
import akka.persistence.typed.scaladsl.PersistentBehaviors.CommandHandler
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Sink
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import hmda.messages.projection.CommonProjectionMessages._

import scala.concurrent.duration._

object InstitutionDBProjector {

  final val name = "InstitutionDBProjector"

  case class StartStreaming()

  case class InstitutionDBProjectorState(offset: Long = 0L) {
    def isEmpty: Boolean = offset == 0L
  }

  val config = ConfigFactory.load()
  val duration = config.getInt("hmda.institution.timeout")

  implicit val timeout = Timeout(duration.seconds)

  //TODO: finish implementation of resumable projection
//  val streamMessages: Behavior[InstitutionDBProjectorCommand] =
//    Behaviors.receive { (ctx, msg) =>
//      implicit val untypedSystem: ActorSystem = ctx.system.toUntyped
//      implicit val materializer: ActorMaterializer = ActorMaterializer()
//      ctx.log.info("message received {}", msg)
//      msg match {
//        case StartStreaming() =>
//          ctx.log.info(s"Start streaming messages for $name")
//          readJournal(untypedSystem)
//            .eventsByTag("institution", Offset.noOffset)
//            .runForeach { e =>
//              println(e)
//            }
//      }
//      Behaviors.same
//    }

  def behavior: Behavior[ProjectionCommand] =
    PersistentBehaviors
      .receive[ProjectionCommand, ProjectionEvent, InstitutionDBProjectorState](
        persistenceId = name,
        emptyState = InstitutionDBProjectorState(),
        commandHandler = commandHandler,
        eventHandler = eventHandler
      )

  val commandHandler: CommandHandler[ProjectionCommand,
                                     ProjectionEvent,
                                     InstitutionDBProjectorState] = {
    (ctx, state, cmd) =>
      implicit val scheduler: Scheduler = ctx.system.scheduler
      cmd match {

        case StartStreaming() =>
          implicit val system = ctx.system.toUntyped
          implicit val materializer = ActorMaterializer()
          ctx.log.info("Streaming messages to from {}", name)
          readJournal(system)
              .eventsByTag("institution", Sequence(state.offset))
              .map(env => ctx.self ? (ref => SaveOffset(env.sequenceNr, ref.asInstanceOf[ActorRef[OffsetSaved]])))
              .runWith(Sink.ignore)
          Effect.none

        case SaveOffset(seqNr, replyTo) =>
          Effect.persist(OffsetSaved(seqNr)).andThen {
            ctx.log.debug("Offset saved: {}", seqNr)
            replyTo ! OffsetSaved(seqNr)
          }

        case GetOffset(replyTo) =>
          replyTo ! OffsetSaved(state.offset)
          Effect.none

      }
  }

  val eventHandler: (InstitutionDBProjectorState,
                     ProjectionEvent) => InstitutionDBProjectorState = {
    case (state, OffsetSaved(seqNr)) => state.copy(offset = seqNr)
  }

}
