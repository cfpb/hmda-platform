package hmda.api.protocol.public

import hmda.model.institution.HmdaFiler
import spray.json.DefaultJsonProtocol

trait HmdaFilerProtocol extends DefaultJsonProtocol {
  implicit val hmdaFilerFormat = jsonFormat4(HmdaFiler.apply)
}
