package hmda.persistence.serialization.upload

import hmda.persistence.messages.events.processing.FileUploadEvents.{ FileNameAdded, LineAdded }
import hmda.persistence.model.serialization.FileUpload.{ FileNameAddedMessage, LineAddedMessage }

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

  def fileNameAddedToProtobuf(obj: FileNameAdded): FileNameAddedMessage = {
    FileNameAddedMessage(
      fileName = obj.fileName
    )
  }

  def fileNameAddedFromProtobuf(msg: FileNameAddedMessage): FileNameAdded = {
    FileNameAdded(
      fileName = msg.fileName
    )
  }

}
