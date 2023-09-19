package hmda.serialization.validation

import akka.actor.typed.ActorRefResolver
import hmda.messages.submission.ValidationProgressTrackerCommands
import hmda.model.processing.state.{ValidationProgress, ValidationProgressTrackerState}
import hmda.persistence.serialization.validationProgressTracker.{ValidationProgressMessage, ValidationProgressTrackerPollMessage, ValidationProgressTrackerStateMessage, ValidationProgressTrackerSubscribeMessage}

object ValidationProgressTrackerCommandsProtobufConverter {
  def subscribeToProtobuf(cmd: ValidationProgressTrackerCommands.Subscribe, refResolver: ActorRefResolver): ValidationProgressTrackerSubscribeMessage =
    ValidationProgressTrackerSubscribeMessage(reply = refResolver.toSerializationFormat(cmd.reply))

  def pollToProtobuf(cmd: ValidationProgressTrackerCommands.Poll, refResolver: ActorRefResolver): ValidationProgressTrackerPollMessage =
    ValidationProgressTrackerPollMessage(reply = refResolver.toSerializationFormat(cmd.reply))

  def stateToProtobuf(cmd: ValidationProgressTrackerState): ValidationProgressTrackerStateMessage =
    ValidationProgressTrackerStateMessage(
      syntacticalValidation = validationProgressToProtobuf(cmd.syntacticalValidation),
      syntacticalValidationProgressPercentage = validationProgressPercentageToProtobuf(cmd.syntacticalValidation),
      syntacticalEdits = cmd.syntacticalEdits.toList,
      qualityValidation = validationProgressToProtobuf(cmd.qualityValidation),
      qualityValidationProgressPercentage = validationProgressPercentageToProtobuf(cmd.qualityValidation),
      qualityEdits = cmd.qualityEdits.toList,
      macroValidation = validationProgressToProtobuf(cmd.macroValidation),
      macroValidationProgressPercentage = validationProgressPercentageToProtobuf(cmd.macroValidation),
      macroEdits = cmd.macroEdits.toList
    )

  private def validationProgressToProtobuf(p: ValidationProgress): ValidationProgressMessage = p match {
    case ValidationProgress.Waiting => ValidationProgressMessage.WAITING
    case ValidationProgress.InProgress(_) => ValidationProgressMessage.IN_PROGRESS
    case ValidationProgress.Completed => ValidationProgressMessage.COMPLETED
    case ValidationProgress.CompletedWithErrors => ValidationProgressMessage.COMPLETED_WITH_ERRORS
  }

  private def validationProgressPercentageToProtobuf(p: ValidationProgress): Int = p match {
    case ValidationProgress.InProgress(percentage) => percentage
    case ValidationProgress.Waiting                => 0
    case ValidationProgress.Completed              => 100
    case ValidationProgress.CompletedWithErrors    => 100
    case _ => 0

  }

  //

  def subscribeFromProtobuf(msg: ValidationProgressTrackerSubscribeMessage, refResolver: ActorRefResolver): ValidationProgressTrackerCommands.Subscribe =
    ValidationProgressTrackerCommands.Subscribe(reply = refResolver.resolveActorRef(msg.reply))

  def pollFromProtobuf(msg: ValidationProgressTrackerPollMessage, refResolver: ActorRefResolver): ValidationProgressTrackerCommands.Poll =
    ValidationProgressTrackerCommands.Poll(reply = refResolver.resolveActorRef(msg.reply))

  def stateFromProtobuf(msg: ValidationProgressTrackerStateMessage): ValidationProgressTrackerState =
    ValidationProgressTrackerState(
      syntacticalValidation = validationProgressFromProtobuf(msg.syntacticalValidation, msg.syntacticalValidationProgressPercentage),
      syntacticalEdits = msg.syntacticalEdits.toSet,
      qualityValidation = validationProgressFromProtobuf(msg.qualityValidation, msg.qualityValidationProgressPercentage),
      qualityEdits = msg.qualityEdits.toSet,
      macroValidation = validationProgressFromProtobuf(msg.macroValidation, msg.macroValidationProgressPercentage),
      macroEdits = msg.macroEdits.toSet
    )

  private def validationProgressFromProtobuf(msg: ValidationProgressMessage, percentage: Int): ValidationProgress = msg match {
    case ValidationProgressMessage.WAITING => ValidationProgress.Waiting
    case ValidationProgressMessage.IN_PROGRESS => ValidationProgress.InProgress(percentage)
    case ValidationProgressMessage.COMPLETED => ValidationProgress.Completed
    case ValidationProgressMessage.COMPLETED_WITH_ERRORS => ValidationProgress.CompletedWithErrors
  }
}