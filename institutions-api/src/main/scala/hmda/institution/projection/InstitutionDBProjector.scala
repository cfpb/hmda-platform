package hmda.institution.projection

import akka.actor.ActorSystem
import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import hmda.query.HmdaQuery._
import akka.actor.typed.scaladsl.adapter._
import akka.persistence.query.{Offset, Sequence}
import akka.persistence.typed.scaladsl.{Effect, PersistentBehaviors}
import akka.persistence.typed.scaladsl.PersistentBehaviors.CommandHandler
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Sink
import hmda.messages.projection.CommonProjectionMessages._

object InstitutionDBProjector {

  final val name = "InstitutionDBProjector"

  case class InstitutionDBProjectorState(offset: Long = 0L) {
    def isEmpty: Boolean = offset == 0L
  }

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
      cmd match {
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
