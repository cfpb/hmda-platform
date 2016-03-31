package hmda.api.protocol.processing

import hmda.model.messages.ProcessingStatus
import spray.json.DefaultJsonProtocol

trait ProcessingStatusProtocol extends DefaultJsonProtocol {
  implicit val processingStatusProtocol = jsonFormat3(ProcessingStatus.apply)
}
