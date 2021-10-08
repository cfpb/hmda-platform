package hmda.messages.submission

import akka.actor.typed.ActorRef
import hmda.messages.CommonMessages.Command
import hmda.model.processing.state._


object ValidationProgressTrackerCommands {
  sealed trait ValidationProgressTrackerCommand                   extends Command
  final case class StateSnapshot(state: HmdaValidationErrorState) extends ValidationProgressTrackerCommand
  final case class ValidationDelta(validationType: ValidationType, validationProgress: ValidationProgress, editNames: Set[String])
    extends ValidationProgressTrackerCommand
  final case class Subscribe(reply: ActorRef[ValidationProgressTrackerState]) extends ValidationProgressTrackerCommand
  final case class Poll(reply: ActorRef[ValidationProgressTrackerState])      extends ValidationProgressTrackerCommand
}