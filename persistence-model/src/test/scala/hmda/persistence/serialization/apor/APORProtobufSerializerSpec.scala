package hmda.persistence.serialization.apor

import org.scalatest.{ MustMatchers, PropSpec }
import org.scalatest.prop.PropertyChecks
import hmda.model.apor.APORGenerator._
import hmda.persistence.messages.commands.apor.APORCommands.CreateApor
import hmda.persistence.messages.events.apor.APOREvents.AporCreated
import hmda.persistence.serialization.apor.CalculateRateSpreadGenerator._

class APORProtobufSerializerSpec extends PropSpec with PropertyChecks with MustMatchers {
  val serializer = new APORProtobufSerializer()

  property("Create APOR messages must be serialized to binary and back") {
    forAll(APORGen, rateTypeGen) { (apor, rateType) =>
      val msg = CreateApor(apor, rateType)
      val bytes = serializer.toBinary(msg)
      serializer.fromBinary(bytes, serializer.CreateAporManifest) mustBe msg

    }
  }

  property("APOR Created messages must be serialized to binary and back") {
    forAll(APORGen, rateTypeGen) { (apor, rateType) =>
      val msg = AporCreated(apor, rateType)
      val bytes = serializer.toBinary(msg)
      serializer.fromBinary(bytes, serializer.AporCreatedManifest) mustBe msg
    }
  }

  property("Calculate Message Spread must be serialized to binary and back") {
    forAll(calculateRateSpreadGen) { calculateRateSpread =>
      val bytes = serializer.toBinary(calculateRateSpread)
      serializer.fromBinary(bytes, serializer.CalculateRateSpreadManifest) mustBe calculateRateSpread
    }
  }

}
