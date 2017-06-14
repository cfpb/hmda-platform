package hmda.apiModel.protocol

import spray.json.DefaultJsonProtocol
import hmda.apiModel.model.Status

trait HmdaApiProtocol extends DefaultJsonProtocol {
  implicit val statusFormat = jsonFormat4(Status.apply)
}
