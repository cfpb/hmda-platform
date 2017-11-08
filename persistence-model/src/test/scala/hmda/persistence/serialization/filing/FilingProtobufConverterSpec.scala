package hmda.persistence.serialization.filing

import org.scalatest.prop.PropertyChecks
import org.scalatest.{ MustMatchers, PropSpec }
import hmda.model.institution.FilingGenerators._
import hmda.persistence.messages.commands.filing.FilingCommands.{ CreateFiling, GetFilingByPeriod, UpdateFilingStatus }
import hmda.persistence.messages.events.institutions.FilingEvents.{ FilingCreated, FilingStatusUpdated }
import hmda.persistence.model.serialization.FilingCommands.{ CreateFilingMessage, GetFilingByPeriodMessage, UpdateFilingStatusMessage }
import hmda.persistence.model.serialization.FilingEvents.{ FilingCreatedMessage, FilingMessage, FilingStatusMessage, FilingStatusUpdatedMessage }
import hmda.persistence.serialization.filing.FilingProtobufConverter._

class FilingProtobufConverterSpec extends PropSpec with PropertyChecks with MustMatchers {

  property("Filing Status must serialize to protobuf and back") {
    forAll(filingStatusGen) { filingStatus =>
      val protobuf = filingStatusToProtobuf(filingStatus).toByteArray
      filingStatusFromProtobuf(FilingStatusMessage.parseFrom(protobuf)) mustBe filingStatus
    }
  }

  property("Filing must serialize to protobuf and back") {
    forAll(filingGen) { filing =>
      val protobuf = filingToProtobuf(filing).toByteArray
      filingFromProtobuf(FilingMessage.parseFrom(protobuf)) mustBe filing
    }
  }

  property("Filing Created must serialize to protobuf and back") {
    forAll(filingGen) { filing =>
      val filingCreated = FilingCreated(filing)
      val protobuf = filingCreatedToProtobuf(filingCreated).toByteArray
      filingCreatedFromProtobuf(FilingCreatedMessage.parseFrom(protobuf)) mustBe filingCreated
    }
  }

  property("Filing Status Updated must serialize to protobuf and back") {
    forAll(filingGen) { filing =>
      val filingStatusUpdated = FilingStatusUpdated(filing)
      val protobuf = filingStatusUpdatedToProtobuf(filingStatusUpdated).toByteArray
      filingStatusUpdatedFromProtobuf(FilingStatusUpdatedMessage.parseFrom(protobuf)) mustBe filingStatusUpdated
    }
  }

  property("Create Filing must serialize to protobuf and back") {
    forAll(filingGen) { filing =>
      val createFiling = CreateFiling(filing)
      val protobuf = createFilingToProtobuf(createFiling).toByteArray
      createFilingFromProtobuf(CreateFilingMessage.parseFrom(protobuf)) mustBe createFiling
    }
  }

  property("Update Status must serialize to protobuf and back") {
    forAll(filingGen) { filing =>
      val updateFilingStatus = UpdateFilingStatus(filing.period, filing.status)
      val protobuf = updateFilingStatusToProtobuf(updateFilingStatus).toByteArray
      updateFilingStatusFromProtobuf(UpdateFilingStatusMessage.parseFrom(protobuf)) mustBe updateFilingStatus
    }
  }

  property("Get Filing by period must serialize to protobuf and back") {
    forAll(filingGen) { filing =>
      val getFilingByPeriod = GetFilingByPeriod(filing.period)
      val protobuf = getFilingByPeriodToProtobuf(getFilingByPeriod).toByteArray
      getFilingByPeriodFromProtobuf(GetFilingByPeriodMessage.parseFrom(protobuf)) mustBe getFilingByPeriod
    }
  }
}
