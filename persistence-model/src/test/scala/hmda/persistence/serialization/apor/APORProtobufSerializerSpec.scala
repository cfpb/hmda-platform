package hmda.persistence.serialization.apor

import org.scalatest.{ MustMatchers, PropSpec }
import org.scalatest.prop.PropertyChecks
import hmda.model.apor.APORGenerator._
import hmda.persistence.messages.commands.apor.APORCommands.CreateApor
import hmda.persistence.messages.events.apor.APOREvents.AporCreated

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

}
