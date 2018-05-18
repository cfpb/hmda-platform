package hmda.api.protocol.public

import hmda.api.model.public.{ HmdaFilerResponse, MsaMd, MsaMdResponse }
import hmda.model.institution.HmdaFiler
import spray.json.DefaultJsonProtocol

trait HmdaFilerProtocol extends DefaultJsonProtocol {
  implicit val hmdaFilerFormat = jsonFormat4(HmdaFiler.apply)
  implicit val hmdaFilerResponseFormat = jsonFormat1(HmdaFilerResponse.apply)
  implicit val msaMdFormat = jsonFormat2(MsaMd.apply)
  implicit val msaMdResponseFormat = jsonFormat2(MsaMdResponse.apply)
}
