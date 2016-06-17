package hmda.api.protocol.processing

import hmda.api.model.processing.{ Institution, Institutions, ProcessingStatus }
import spray.json.DefaultJsonProtocol

trait ProcessingProtocol extends DefaultJsonProtocol {
  implicit val processingStatusFormat = jsonFormat2(ProcessingStatus.apply)
  implicit val institutionFormat = jsonFormat5(Institution.apply)
  implicit val institutionsFormat = jsonFormat1(Institutions.apply)
}