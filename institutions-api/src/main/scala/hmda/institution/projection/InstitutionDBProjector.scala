package hmda.institution.projection

import akka.actor.ActorSystem
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import hmda.query.HmdaQuery._
import akka.actor.typed.scaladsl.adapter._
import akka.persistence.query.{Offset, Sequence}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Sink

object InstitutionDBProjector {

  final val name = "InstitutionDBProjector"

  sealed trait InstitutionDBProjectorCommand
  sealed trait InstitutionDBProjectionEvent

  final case class StartStreaming() extends InstitutionDBProjectorCommand

  val streamMessages: Behavior[InstitutionDBProjectorCommand] =
    Behaviors.receive { (ctx, msg) =>
      implicit val untypedSystem: ActorSystem = ctx.system.toUntyped
      implicit val materializer: ActorMaterializer = ActorMaterializer()
      ctx.log.info("message received {}", msg)
      msg match {
        case StartStreaming() =>
          ctx.log.info(s"Start streaming messages for $name")
          //eventEnvelopeByTag("Institution", Sequence(0L)).runForeach(e =>
          readJournal(untypedSystem)
            .eventsByTag("institution", Offset.noOffset)
            //.eventsByPersistenceId(
            //  "Institution-Institution-54930084UKLVMY22DS16",
            //  0L,
            //  Long.MaxValue)
            .runForeach { e =>
              println(e)
            }
//            .map { e =>
//              ctx.log.info(e.offset.toString)
//            }
//            .runWith(Sink.ignore)
      }
      Behaviors.same
    }

}
