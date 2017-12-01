package hmda.api.protocol.apor

import hmda.persistence.messages.commands.apor.APORCommands.CalculateRateSpread
import org.scalatest.{ MustMatchers, PropSpec }
import org.scalatest.prop.PropertyChecks
import spray.json._
import hmda.persistence.serialization.apor.CalculateRateSpreadGenerator
import hmda.api.protocol.apor.RateSpreadProtocol._

class RateSpreadProtocolSpec extends PropSpec with PropertyChecks with CalculateRateSpreadGenerator with MustMatchers {
  forAll(calculateRateSpreadGen) { c =>
    c.toJson.convertTo[CalculateRateSpread] mustBe c
  }
}
