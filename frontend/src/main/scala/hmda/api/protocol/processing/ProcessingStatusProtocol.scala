package hmda.api.protocol.processing

import hmda.model.messages.{ ProcessingStatus, ProcessingStatusSeq }
import spray.json.DefaultJsonProtocol

trait ProcessingStatusProtocol extends DefaultJsonProtocol {
  implicit val processingStatusFormat = jsonFormat3(ProcessingStatus.apply)
  implicit val processingStatusSeqFormat = jsonFormat1(ProcessingStatusSeq.apply)
}
