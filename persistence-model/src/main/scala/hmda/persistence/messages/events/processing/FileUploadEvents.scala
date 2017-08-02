package hmda.persistence.messages.events.processing

import hmda.persistence.messages.CommonMessages.Event

object FileUploadEvents {
  case class LineAdded(timestamp: Long, data: String) extends Event
  case class FileNameAdded(fileName: String) extends Event
}
