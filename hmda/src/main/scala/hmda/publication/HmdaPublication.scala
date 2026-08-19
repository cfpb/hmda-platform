package hmda.publication

import org.apache.pekko.actor.typed.{ Behavior, PostStop, PreRestart }
import org.apache.pekko.actor.typed.scaladsl.Behaviors

object HmdaPublication {

  final val name = "HmdaPublication"

  sealed trait HmdaPublicationCommand
  case object StopHmdaPublication extends HmdaPublicationCommand

  def apply(): Behavior[HmdaPublicationCommand] =
    Behaviors.setup { ctx =>
      ctx.log.info(s"Actor started at ${ctx.self.path}")
      Behaviors
        .receive[HmdaPublicationCommand] {
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