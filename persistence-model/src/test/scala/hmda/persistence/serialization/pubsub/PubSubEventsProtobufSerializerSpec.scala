package hmda.persistence.serialization.pubsub

import org.scalatest.{ MustMatchers, PropSpec }
import org.scalatest.prop.PropertyChecks
import hmda.model.institution.SubmissionGenerators._
import hmda.persistence.messages.commands.publication.PublicationCommands.{ GenerateAggregateReports, GenerateDisclosureReports }
import hmda.persistence.messages.events.pubsub.PubSubEvents._

class PubSubEventsProtobufSerializerSpec extends PropSpec with PropertyChecks with MustMatchers {

  val serializer = new PubSubEventsProtobufSerializer()

  property("Submission Signed PubSub messages must be serialized to binary and back") {
    forAll(submissionIdGen) { submissionId =>
      val msg = SubmissionSignedPubSub(submissionId)
      val protobuf = serializer.toBinary(msg)
      serializer.fromBinary(protobuf, serializer.SubmissionSignedPubSubManifest) mustBe msg
    }
  }

  property("FindAggregatePublisher messages must be serialized to binary and back") {
    val msg = FindAggregatePublisher()
    val protobuf = serializer.toBinary(msg)
    serializer.fromBinary(protobuf, serializer.FindAggregatePublisherManifest) mustBe msg
  }

  property("FindDisclosurePublisher messages must be serialized to binary and back") {
    val msg = FindDisclosurePublisher()
    val protobuf = serializer.toBinary(msg)
    serializer.fromBinary(protobuf, serializer.FindDisclosurePublisherManifest) mustBe msg
  }

  property("GenerateAggregateReports messages must be serialized to binary and back") {
    val msg = GenerateAggregateReports()
    val protobuf = serializer.toBinary(msg)
    serializer.fromBinary(protobuf, serializer.GenerateAggregateReportsManifest) mustBe msg
  }

  property("GenerateDisclosureReports messages must be serialized to binary and back") {
    forAll(submissionIdGen) { submissionId =>
      val msg = GenerateDisclosureReports(submissionId)
      val protobuf = serializer.toBinary(msg)
      serializer.fromBinary(protobuf, serializer.GenerateDisclosureReportsManifest) mustBe msg
    }
  }
}
