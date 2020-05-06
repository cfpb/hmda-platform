package hmda.validation

import akka.actor.typed.{ Behavior, PostStop, PreRestart }
import akka.actor.typed.scaladsl.Behaviors

object HmdaValidation {

  final val name = "HmdaValidation"

  sealed trait HmdaValidationCommand
  case object StopHmdaValidation extends HmdaValidationCommand

  def apply(): Behavior[HmdaValidationCommand] =
    Behaviors.setup { ctx =>
      ctx.log.info(s"Actor started at ${ctx.self.path}")
      Behaviors
        .receive[HmdaValidationCommand] {
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