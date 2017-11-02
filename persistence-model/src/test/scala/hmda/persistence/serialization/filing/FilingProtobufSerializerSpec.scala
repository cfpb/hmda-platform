package hmda.persistence.serialization.filing

import org.scalatest.{ MustMatchers, PropSpec }
import org.scalatest.prop.PropertyChecks
import hmda.model.institution.FilingGenerators._
import hmda.persistence.messages.commands.filing.FilingCommands.{ CreateFiling, GetFilingByPeriod, UpdateFilingStatus }
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

  property("Filing messages must be serialized to binary and back") {
    forAll(filingGen) { filing =>
      val bytes = serializer.toBinary(filing)
      serializer.fromBinary(bytes, serializer.FilingManifest) mustBe filing
    }
  }

  property("Create Filing messages must be serialized to binary and back") {
    forAll(filingGen) { filing =>
      val msg = CreateFiling(filing)
      val bytes = serializer.toBinary(msg)
      serializer.fromBinary(bytes, serializer.CreateFilingManifest) mustBe msg
    }
  }

  property("Update Filing Status messages must be serialized to binary and back") {
    forAll(filingGen) { filing =>
      val msg = UpdateFilingStatus(filing.period, filing.status)
      val bytes = serializer.toBinary(msg)
      serializer.fromBinary(bytes, serializer.UpdateFilingStatusManifest) mustBe msg
    }
  }

  property("Get Filing by Period messages must be serialized to binary and back") {
    forAll(filingGen) { filing =>
      val msg = GetFilingByPeriod(filing.period)
      val bytes = serializer.toBinary(msg)
      serializer.fromBinary(bytes, serializer.GetFilingByPeriodManifest) mustBe msg
    }
  }

}
