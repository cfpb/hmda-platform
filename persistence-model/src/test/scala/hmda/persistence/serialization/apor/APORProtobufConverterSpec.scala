package hmda.persistence.serialization.apor

import org.scalatest.{ MustMatchers, PropSpec }
import org.scalatest.prop.PropertyChecks
import hmda.model.apor.APORGenerator._
import hmda.persistence.messages.commands.apor.APORCommands.CreateApor
import hmda.persistence.messages.events.apor.APOREvents.AporCreated
import hmda.persistence.model.serialization.APOR.APORMessage
import hmda.persistence.model.serialization.APORCommands.CreateAPORMessage
import hmda.persistence.model.serialization.APOREvents.APORCreatedMessage
import hmda.persistence.serialization.apor.APORProtobufConverter._

class APORProtobufConverterSpec extends PropSpec with PropertyChecks with MustMatchers {

  property("APOR must serialize to protobuf and back") {
    forAll(APORGen) { apor =>
      val protobuf = aporToProtobuf(apor).toByteArray
      aporFromProtobuf(APORMessage.parseFrom(protobuf)) mustBe apor
    }
  }

  property("Create APOR must serialize to protobuf and back") {
    forAll(APORGen, rateTypeGen) { (apor, rateType) =>
      val createApor = CreateApor(apor, rateType)
      val protobuf = createAporToProtobuf(createApor).toByteArray
      createAporFromProtobuf(CreateAPORMessage.parseFrom(protobuf)) mustBe createApor
    }
  }

  property("APOR Created must serialize to protobuf and back") {
    forAll(APORGen, rateTypeGen) { (apor, rateType) =>
      val aporCreated = AporCreated(apor, rateType)
      val protobuf = aporCreatedToProtobuf(aporCreated).toByteArray
      aporCreatedFromProtobuf(APORCreatedMessage.parseFrom(protobuf)) mustBe aporCreated
    }
  }
}
