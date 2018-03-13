package hmda.persistence.serialization.pubsub

import org.scalatest.{ MustMatchers, PropSpec }
import org.scalatest.prop.PropertyChecks
import hmda.model.institution.SubmissionGenerators._
import hmda.persistence.messages.commands.publication.PublicationCommands.{ GenerateAggregateReports, GenerateDisclosureReports }
import hmda.persistence.messages.events.pubsub.PubSubEvents._
import hmda.persistence.model.serialization.PubSubEvents._
import hmda.persistence.serialization.pubsub.PubSubEventsProtobufConverter._

class PubSubEventsProtobufConverterSpec extends PropSpec with PropertyChecks with MustMatchers {

  property("Submission Signed pubsub event must serialize to protobuf and back") {
    forAll(submissionIdGen) { submissionId =>
      val submissionSigned = SubmissionSignedPubSub(submissionId)
      val protobuf = submissionSignedPubSubToProtobuf(submissionSigned).toByteArray
      submissionSignedPubSubFromProtobuf(SubmissionSignedPubSubMessage.parseFrom(protobuf)) mustBe SubmissionSignedPubSub(submissionId)
    }
  }

  property("Find Aggregate Publisher must serialize to protobuf and back") {
    val find = FindAggregatePublisher()
    val protobuf = findAggregatePublisherToProtobuf(find).toByteArray
    findAggregatePublisherFromProtobuf(FindAggregatePublisherMessage.parseFrom(protobuf)) mustBe FindAggregatePublisher()
  }

  property("Find Disclosure Publisher must serialize to protobuf and back") {
    val find = FindDisclosurePublisher()
    val protobuf = findDisclosurePublisherToProtobuf(find).toByteArray
    findDisclosurePublisherFromProtobuf(FindDisclosurePublisherMessage.parseFrom(protobuf)) mustBe FindDisclosurePublisher()
  }

  property("Generate Aggregate Reports must serialize to protobuf and back") {
    val generate = GenerateAggregateReports()
    val protobuf = generateAggregateReportsToProtobuf(generate).toByteArray
    generateAggregateReportsFromProtobuf(GenerateAggregateReportsMessage.parseFrom(protobuf)) mustBe GenerateAggregateReports()
  }

  property("Generate Disclosure Reports must serialize to protobuf and back") {
    forAll(submissionIdGen) { submissionId =>
      val generate = GenerateDisclosureReports(submissionId)
      val protobuf = generateDisclosureReportsToProtobuf(generate).toByteArray
      generateDisclosureReportsFromProtobuf(GenerateDisclosureReportsMessage.parseFrom(protobuf)) mustBe GenerateDisclosureReports(submissionId)
    }
  }

}
