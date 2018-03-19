package hmda.persistence.serialization.filing

import org.scalatest.{ MustMatchers, PropSpec }
import org.scalatest.prop.PropertyChecks
import hmda.model.institution.FilingGenerators._
import hmda.persistence.messages.commands.institutions.HmdaFilerCommands.{ CreateHmdaFiler, DeleteHmdaFiler, FindHmdaFiler }
import hmda.persistence.messages.events.institutions.HmdaFilerEvents.{ HmdaFilerCreated, HmdaFilerDeleted }

class HmdFilerProtobufSerializerSpec extends PropSpec with PropertyChecks with MustMatchers {

  val serializer = new HmdaFilerProtobufSerializer()

  property("CreateHmdaFiler messages must be serialized to binary and back") {
    forAll(hmdaFilerGen) { hmdaFiler =>
      val msg = CreateHmdaFiler(hmdaFiler)
      val bytes = serializer.toBinary(msg)
      serializer.fromBinary(bytes, serializer.CreateHmdaFilerManifest) mustBe msg
    }
  }

  property("DeleteHmdaFiler messages must be serialized to binary and back") {
    forAll(hmdaFilerGen) { hmdaFiler =>
      val msg = DeleteHmdaFiler(hmdaFiler)
      val bytes = serializer.toBinary(msg)
      serializer.fromBinary(bytes, serializer.DeleteHmdaFilerManifest) mustBe msg
    }
  }

  property("FindHmdaFiler messages must be serialized to binary and back") {
    forAll(hmdaFilerGen) { hmdaFiler =>
      val msg = FindHmdaFiler(hmdaFiler.institutionId)
      val bytes = serializer.toBinary(msg)
      serializer.fromBinary(bytes, serializer.FindHmdaFilerManifest) mustBe msg
    }
  }

  property("HmdaFilerCreated messages must be serialized to binary and back") {
    forAll(hmdaFilerGen) { hmdaFiler =>
      val msg = HmdaFilerCreated(hmdaFiler)
      val bytes = serializer.toBinary(msg)
      serializer.fromBinary(bytes, serializer.HmdaFilerCreatedManifest) mustBe msg
    }
  }

  property("HmdaFilerDeleted messages must be serialized to binary and back") {
    forAll(hmdaFilerGen) { hmdaFiler =>
      val msg = HmdaFilerDeleted(hmdaFiler)
      val bytes = serializer.toBinary(msg)
      serializer.fromBinary(bytes, serializer.HmdaFilerDeletedManifest) mustBe msg
    }
  }

}
