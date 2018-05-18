package hmda.publication

import akka.actor.typed.{Behavior, PostStop, PreRestart}
import akka.actor.typed.scaladsl.Behaviors

object HmdaPublication {

  final val name = "HmdaPublication"

  sealed trait HmdaPublicationMessage
  case object StopHmdaPublication extends HmdaPublicationMessage

  val behavior: Behavior[HmdaPublicationMessage] =
    Behaviors.setup { ctx =>
      ctx.log.info(s"Actor started at ${ctx.self.path}")
      Behaviors
        .receive[HmdaPublicationMessage] {
          case (_, msg) =>
            msg match {
              case StopHmdaPublication =>
                Behaviors.stopped
            }
        }
        .receiveSignal {
          case (ctx, PreRestart) =>
            ctx.log.info(s"Actor restarted at ${ctx.self.path}")
            Behaviors.same
          case (ctx, PostStop) =>
            ctx.log.info(s"Actor stopped at ${ctx.self.path}")
            Behaviors.same
        }
    }

}
