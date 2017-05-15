package hmda.persistence.serialization.filing

import hmda.model.fi._
import hmda.persistence.messages.events.institutions.FilingEvents.{ FilingCreated, FilingStatusUpdated }
import hmda.persistence.model.serialization.FilingEvents._

object FilingProtobufConverter {

  def filingCreatedToProtobuf(obj: FilingCreated): FilingCreatedMessage = {
    FilingCreatedMessage(
      filing = Some(filingToProtobuf(obj.filing))
    )
  }

  def filingCreatedFromProtobuf(msg: FilingCreatedMessage): FilingCreated = {
    FilingCreated(
      filing = filingFromProtobuf(msg.filing.getOrElse(FilingMessage()))
    )
  }

  def filingStatusUpdatedToProtobuf(obj: FilingStatusUpdated): FilingStatusUpdatedMessage = {
    FilingStatusUpdatedMessage(
      filing = Some(filingToProtobuf(obj.filing))
    )
  }

  def filingStatusUpdatedFromProtobuf(msg: FilingStatusUpdatedMessage): FilingStatusUpdated = {
    FilingStatusUpdated(
      filing = filingFromProtobuf(msg.filing.getOrElse(FilingMessage()))
    )
  }

  def filingToProtobuf(obj: Filing): FilingMessage = {
    FilingMessage(
      period = obj.period,
      institutionId = obj.institutionId,
      status = Some(filingStatusToProtobuf(obj.status)),
      filingRequired = obj.filingRequired,
      start = obj.start,
      end = obj.end
    )
  }

  def filingFromProtobuf(msg: FilingMessage): Filing = {
    Filing(
      period = msg.period,
      institutionId = msg.institutionId,
      status = filingStatusFromProtobuf(msg.status.getOrElse(FilingStatusMessage())),
      filingRequired = msg.filingRequired,
      start = msg.start,
      end = msg.end
    )
  }

  def filingStatusToProtobuf(obj: FilingStatus): FilingStatusMessage = {
    FilingStatusMessage(
      code = obj.code,
      message = obj.message
    )
  }

  def filingStatusFromProtobuf(msg: FilingStatusMessage): FilingStatus = {
    msg.code match {
      case 1 => NotStarted
      case 2 => InProgress
      case 3 => Completed
      case -1 => Cancelled
    }
  }

}
