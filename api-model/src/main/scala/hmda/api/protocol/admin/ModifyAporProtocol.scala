package hmda.api.protocol.admin

import hmda.api.model.admin.ModifyAporRequest
import spray.json.DefaultJsonProtocol
import hmda.api.protocol.apor.RateSpreadProtocol._

object ModifyAporProtocol extends DefaultJsonProtocol {
  implicit val modifyAporFormat = jsonFormat2(ModifyAporRequest.apply)
}
