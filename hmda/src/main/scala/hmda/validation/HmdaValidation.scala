package hmda.validation

import akka.actor.typed.{Behavior, PostStop, PreRestart}
import akka.actor.typed.scaladsl.Behaviors

object HmdaValidation {

  final val name = "HmdaValidation"

  sealed trait HmdaValidationMessage
  case object StopHmdaValidation extends HmdaValidationMessage

  val behavior: Behavior[HmdaValidationMessage] =
    Behaviors.setup { ctx =>
      ctx.log.info(s"Actor started at ${ctx.self.path}")
      Behaviors
        .receive[HmdaValidationMessage] {
          case (_, msg) =>
            msg match {
              case StopHmdaValidation =>
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
