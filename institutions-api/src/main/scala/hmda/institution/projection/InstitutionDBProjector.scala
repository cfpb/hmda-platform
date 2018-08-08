package hmda.institution.projection

import akka.actor.ActorSystem
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import hmda.query.HmdaQuery._
import akka.actor.typed.scaladsl.adapter._
import akka.persistence.query.{Offset, Sequence}
import akka.persistence.typed.scaladsl.PersistentBehaviors
import akka.persistence.typed.scaladsl.PersistentBehaviors.CommandHandler
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Sink

object InstitutionDBProjector {

  final val name = "InstitutionDBProjector"

  sealed trait InstitutionDBProjectorCommand
  sealed trait InstitutionDBProjectionEvent

  final case class StartStreaming() extends InstitutionDBProjectorCommand
  final case class SaveOffset(offset: Offset)
      extends InstitutionDBProjectorCommand
  final case class OffsetSaved(seqNr: Long) extends InstitutionDBProjectionEvent

  case class InstitutionDBProjectorState(offset: Long) {
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

  def behavior: Behavior[InstitutionDBProjectorCommand] =
    PersistentBehaviors
      .receive[InstitutionDBProjectorCommand,
               InstitutionDBProjectionEvent,
               InstitutionDBProjectorState](
        persistenceId = name,
        emptyState = InstitutionDBProjectorState(),
        commandHandler = commandHandler,
        eventHandler = eventHandler
      )

  val commandHandler: CommandHandler[InstitutionDBProjectorCommand,
                                     InstitutionDBProjectionEvent,
                                     InstitutionDBProjectorState] = ???


  val eventHandler
    : (InstitutionDBProjectorState,
       InstitutionDBProjectionEvent) => InstitutionDBProjectorState = ???

}
