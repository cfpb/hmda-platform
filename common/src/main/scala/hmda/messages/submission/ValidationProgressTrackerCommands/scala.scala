package hmda.messages.submission

import akka.actor.typed.ActorRef
import hmda.messages.CommonMessages.Command
import hmda.model.processing.state.{
  HmdaValidationErrorState,
  ValidationProgress,
  ValidationProgressTrackerState,
  ValidationType,
  VerificationType
}

object ValidationProgressTrackerCommands {
  sealed trait ValidationProgressTrackerCommand                   extends Command
  final case class StateSnapshot(state: HmdaValidationErrorState) extends ValidationProgressTrackerCommand
  final case class ValidationDelta(validationType: ValidationType, validationProgress: ValidationProgress)
    extends ValidationProgressTrackerCommand
  final case class NumberOfLines(lines: Long)                                                     extends ValidationProgressTrackerCommand
  final case class VerifyDelta(verificationType: VerificationType, verificationProgress: Boolean) extends ValidationProgressTrackerCommand
  final case class SignedDelta(submissionSigned: Boolean)                                         extends ValidationProgressTrackerCommand
  final case class Subscribe(reply: ActorRef[ValidationProgressTrackerState])                     extends ValidationProgressTrackerCommand
  final case class Poll(reply: ActorRef[ValidationProgressTrackerState])                          extends ValidationProgressTrackerCommand
}