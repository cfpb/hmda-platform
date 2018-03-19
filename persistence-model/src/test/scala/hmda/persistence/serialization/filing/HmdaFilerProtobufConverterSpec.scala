package hmda.persistence.serialization.filing

import hmda.model.institution.FilingGenerators._
import org.scalatest.{ MustMatchers, PropSpec }
import org.scalatest.prop.PropertyChecks
import HmdaFilerProtobufConverter._
import hmda.persistence.messages.commands.institutions.HmdaFilerCommands.{ CreateHmdaFiler, DeleteHmdaFiler, FindHmdaFiler }
import hmda.persistence.messages.events.institutions.HmdaFilerEvents.{ HmdaFilerCreated, HmdaFilerDeleted }
import hmda.persistence.model.serialization.HmdaFiler.HmdaFilerMessage
import hmda.persistence.model.serialization.HmdaFilerCommands.{ CreateHmdaFilerMessage, DeleteHmdaFilerMessage, FindHmdaFilerMessage }
import hmda.persistence.model.serialization.HmdaFilerEvents.{ HmdaFilerCreatedMessage, HmdaFilerDeletedMessage }

class HmdaFilerProtobufConverterSpec extends PropSpec with PropertyChecks with MustMatchers {

  property("HmdaFiler must serialize to protobuf and back") {
    forAll(hmdaFilerGen) { hmdaFiler =>
      val protobuf = hmdaFilerToProtobuf(hmdaFiler).toByteArray
      hmdaFilerFromProtobuf(HmdaFilerMessage.parseFrom(protobuf)) mustBe hmdaFiler
    }
  }

  property("HmdaFilerCreate must serialize to protobuf and back") {
    forAll(hmdaFilerGen) { hmdaFiler =>
      val protobuf = createHmdaFilerToProtobuf(CreateHmdaFiler(hmdaFiler)).toByteArray
      createHmdaFilerFromProtobuf(CreateHmdaFilerMessage.parseFrom(protobuf)) mustBe CreateHmdaFiler(hmdaFiler)
    }
  }

  property("HmdaFilerDelete must serialize to protobuf and back") {
    forAll(hmdaFilerGen) { hmdaFiler =>
      val protobuf = deleteHmdaFilerToProtobuf(DeleteHmdaFiler(hmdaFiler)).toByteArray
      deleteHmdaFilerFromProtobuf(DeleteHmdaFilerMessage.parseFrom(protobuf)) mustBe DeleteHmdaFiler(hmdaFiler)
    }
  }

  property("HmdaFilerCreated must serialize to protobuf and back") {
    forAll(hmdaFilerGen) { hmdaFiler =>
      val protobuf = hmdaFilerCreatedToProtobuf(HmdaFilerCreated(hmdaFiler)).toByteArray
      hmdaFilerCreatedFromProtobuf(HmdaFilerCreatedMessage.parseFrom(protobuf)) mustBe HmdaFilerCreated(hmdaFiler)
    }
  }

  property("HmdaFilerDeleted must serialize to protobuf and back") {
    forAll(hmdaFilerGen) { hmdaFiler =>
      val protobuf = hmdaFilerDeletedToProtobuf(HmdaFilerDeleted(hmdaFiler)).toByteArray
      hmdaFilerDeletedFromProtobuf(HmdaFilerDeletedMessage.parseFrom(protobuf)) mustBe HmdaFilerDeleted(hmdaFiler)
    }
  }

  property("FindHmdaFiler must serialize to protobuf and back") {
    forAll(hmdaFilerGen) { hmdaFiler =>
      val protobuf = findHmdaFilerToProtobuf(FindHmdaFiler(hmdaFiler.institutionId)).toByteArray
      findHmdaFilerFromProtobuf(FindHmdaFilerMessage.parseFrom(protobuf)) mustBe FindHmdaFiler(hmdaFiler.institutionId)
    }
  }

}
