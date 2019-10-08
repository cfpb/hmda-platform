package hmda.serialization.submission

import hmda.messages.submission.SubmissionEvents.{ SubmissionCreated, SubmissionModified, SubmissionNotExists }
import hmda.messages.submission.SubmissionManagerCommands.{ UpdateSubmissionStatus, WrappedSubmissionEventResponse }
import hmda.model.filing.submission.SubmissionId
import hmda.persistence.serialization.submission.SubmissionMessage
import hmda.persistence.serialization.submissionmanager.commands.WrappedSubmissionEventResponseMessage.Sub
import hmda.persistence.serialization.submissionmanager.commands.WrappedSubmissionEventResponseMessage.Sub.{
  SubmissionCreatedField,
  SubmissionModifiedField,
  SubmissionNotExistsField
}
import hmda.persistence.serialization.submissionmanager.commands.{ UpdateSubmissionStatusMessage, WrappedSubmissionEventResponseMessage }
import hmda.serialization.submission.SubmissionEventsProtobufConverter._
import hmda.serialization.submission.SubmissionProtobufConverter._

object SubmissionManagerCommandsProtobufConverter {

  def updateSubmissionStatusToProtobuf(cmd: UpdateSubmissionStatus): UpdateSubmissionStatusMessage =
    UpdateSubmissionStatusMessage(
      submission = Some(submissionToProtobuf(cmd.submission))
    )

  def updateSubmissionStatusFromProtobuf(bytes: Array[Byte]): UpdateSubmissionStatus = {
    val msg        = UpdateSubmissionStatusMessage.parseFrom(bytes)
    val submission = submissionFromProtobuf(msg.submission.getOrElse(SubmissionMessage()))
    UpdateSubmissionStatus(submission)
  }

  def wrappedSubmissionEventResponseToProtobuf(cmd: WrappedSubmissionEventResponse): WrappedSubmissionEventResponseMessage =
    cmd.submissionEvent match {
      case sc: SubmissionCreated =>
        WrappedSubmissionEventResponseMessage(SubmissionCreatedField(submissionCreatedToProtobuf(sc)))
      case sm: SubmissionModified =>
        WrappedSubmissionEventResponseMessage(SubmissionModifiedField(submissionModifiedToProtobuf(sm)))
      case sne: SubmissionNotExists =>
        WrappedSubmissionEventResponseMessage(SubmissionNotExistsField(submissionNotExistsToProtobuf(sne)))
    }

  def wrappedSubmissionEventResponseFromProtobuf(bytes: Array[Byte]): WrappedSubmissionEventResponse = {
    val msg = WrappedSubmissionEventResponseMessage.parseFrom(bytes)
    msg.sub match {
      case Sub.SubmissionCreatedField(sc) =>
        WrappedSubmissionEventResponse(submissionCreatedFromProtobuf(sc))
      case Sub.SubmissionModifiedField(sm) =>
        WrappedSubmissionEventResponse(submissionModifiedFromProtobuf(sm))
      case Sub.SubmissionNotExistsField(sne) =>
        WrappedSubmissionEventResponse(submissionNotExistsFromProtobuf(sne))
      case Sub.Empty =>
        WrappedSubmissionEventResponse(SubmissionNotExists(SubmissionId()))
    }
  }
}
