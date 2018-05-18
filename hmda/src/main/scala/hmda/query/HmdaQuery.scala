package hmda.query

import akka.actor.typed.{Behavior, PostStop, PreRestart}
import akka.actor.typed.scaladsl.Behaviors

object HmdaQuery {

  final val name = "HmdaQuery"

  sealed trait HmdaQueryMessage
  case object StopHmdaQuery extends HmdaQueryMessage

  val behavior: Behavior[HmdaQueryMessage] =
    Behaviors.setup { ctx =>
      ctx.log.info(s"Actor started at ${ctx.self.path}")
      Behaviors
        .receive[HmdaQueryMessage] {
          case (_, msg) =>
            msg match {
              case StopHmdaQuery =>
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
