package hmda.api.protocol.admin

import hmda.api.model.admin.AdminAporRequests.{ CreateAporRequest, ModifyAporRequest }
import spray.json.DefaultJsonProtocol
import hmda.api.protocol.apor.RateSpreadProtocol._
import hmda.persistence.messages.events.apor.APOREvents.{ AporCreated, AporModified }

object AdminAporProtocol extends DefaultJsonProtocol {
  implicit val createAporRequestFormat = jsonFormat2(CreateAporRequest.apply)
  implicit val modifyAporRequestFormat = jsonFormat2(ModifyAporRequest.apply)
  implicit val aporCreatedFormat = jsonFormat2(AporCreated.apply)
  implicit val aporModifiedFormat = jsonFormat2(AporModified.apply)
}
