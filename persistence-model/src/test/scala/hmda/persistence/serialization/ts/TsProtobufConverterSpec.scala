package hmda.persistence.serialization.ts

import hmda.model.fi.ts.TsGenerators
import hmda.persistence.model.serialization.TransmittalSheet.{ ContactMessage, TransmittalSheetMessage, TsParentMessage, TsRespondentMessage }
import hmda.persistence.serialization.ts.TsProtobufConverter._
import org.scalatest.{ MustMatchers, PropSpec }
import org.scalatest.prop.PropertyChecks

class TsProtobufConverterSpec extends PropSpec with PropertyChecks with MustMatchers with TsGenerators {

  property("Transmittal sheet must serialize to protobuf and back") {
    forAll(tsGen) { ts =>
      val protobuf = tsToProtobuf(ts).toByteArray
      tsFromProtobuf(TransmittalSheetMessage.parseFrom(protobuf)) mustBe ts
    }
  }

  property("Respondent must serialize to protobuf and back") {
    forAll(respondentGen) { respondent =>
      val protobuf = tsRespondentToProtobuf(respondent).toByteArray
      tsRespondentFromProtobuf(TsRespondentMessage.parseFrom(protobuf)) mustBe respondent
    }
  }

  property("Parent must serialize to protobuf and back") {
    forAll(parentGen) { parent =>
      val protobuf = tsParentToProtobuf(parent).toByteArray
      tsParentFromProtobuf(TsParentMessage.parseFrom(protobuf)) mustBe parent
    }
  }

  property("Contact must serialize to protobuf and back") {
    forAll(contactGen) { contact =>
      val protobuf = contactToProtobuf(contact).toByteArray
      contactFromProtobuf(ContactMessage.parseFrom(protobuf)) mustBe contact
    }
  }

}
