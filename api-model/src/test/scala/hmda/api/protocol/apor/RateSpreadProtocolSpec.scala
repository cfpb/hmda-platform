package hmda.api.protocol.apor

import hmda.persistence.messages.commands.apor.APORCommands.CalculateRateSpread
import org.scalatest.{ MustMatchers, PropSpec }
import org.scalatest.prop.PropertyChecks
import spray.json._
import hmda.persistence.serialization.apor.CalculateRateSpreadGenerator
import hmda.model.apor.APOR
import hmda.model.apor.APORGenerator._
import hmda.api.protocol.apor.RateSpreadProtocol._

class RateSpreadProtocolSpec extends PropSpec with PropertyChecks with CalculateRateSpreadGenerator with MustMatchers {
  property("APOR must convert to and from json") {
    forAll(APORGen) { apor =>
      apor.toJson.convertTo[APOR] mustBe apor
    }
  }
  property("CalculateRateSpread must convert to and from json") {
    forAll(calculateRateSpreadGen) { c =>
      c.toJson.convertTo[CalculateRateSpread] mustBe c
    }
  }
}
