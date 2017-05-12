package hmda.persistence.serialization.filing

import org.scalatest.{ MustMatchers, PropSpec }
import org.scalatest.prop.PropertyChecks
import hmda.model.institution.FilingGenerators._
import hmda.persistence.messages.events.institutions.FilingEvents.{ FilingCreated, FilingStatusUpdated }

class FilingProtobufSerializerSpec extends PropSpec with PropertyChecks with MustMatchers {
  val serializer = new FilingProtobufSerializer()

  property("Filing Created messages must be serialized to binary and back") {
    forAll(filingGen) { filing =>
      val msg = FilingCreated(filing)
      val bytes = serializer.toBinary(msg)
      serializer.fromBinary(bytes, serializer.FilingCreatedManifest) mustBe msg
    }

  }

  property("Filing Status Updated messages must be serialized to binary and back") {
    forAll(filingGen) { filing =>
      val msg = FilingStatusUpdated(filing)
      val bytes = serializer.toBinary(msg)
      serializer.fromBinary(bytes, serializer.FilingStatusUpdatedManifest) mustBe msg
    }
  }
}
