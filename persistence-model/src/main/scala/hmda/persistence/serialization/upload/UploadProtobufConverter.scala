package hmda.persistence.serialization.upload

import hmda.persistence.messages.events.processing.FileUploadEvents.LineAdded
import hmda.persistence.model.serialization.FileUpload.LineAddedMessage

object UploadProtobufConverter {

  def lineAddedToProtobuf(obj: LineAdded): LineAddedMessage = {
    LineAddedMessage(
      timestamp = obj.timestamp,
      data = obj.data
    )
  }

  def lineAddedFromProtobuf(msg: LineAddedMessage): LineAdded = {
    LineAdded(
      timestamp = msg.timestamp,
      data = msg.data
    )
  }

}
