package hmda.api.protocol

import spray.json.DefaultJsonProtocol
import hmda.api.model.Status

trait HmdaApiProtocol extends DefaultJsonProtocol {
  implicit val statusFormat = jsonFormat4(Status.apply)
}
