package hmda.api.protocol.admin

import hmda.api.model.admin.ModifyAporRequest
import org.scalatest.{ MustMatchers, PropSpec }
import org.scalatest.prop.PropertyChecks
import hmda.model.apor.APORGenerator._
import spray.json._
import hmda.api.protocol.admin.ModifyAporProtocol._

class ModifyAporProtocolSpec extends PropSpec with PropertyChecks with MustMatchers {

  property("Modify APOR Request must convert to and from json") {
    forAll(rateTypeGen, APORGen) { (rateType, apor) =>
      val request = ModifyAporRequest(rateType, apor)
      request.toJson.convertTo[ModifyAporRequest] mustBe request
    }
  }
}
