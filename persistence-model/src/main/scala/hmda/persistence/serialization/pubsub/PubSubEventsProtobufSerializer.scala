package hmda.persistence.serialization.pubsub

import akka.serialization.SerializerWithStringManifest
import hmda.persistence.messages.commands.publication.PublicationCommands.{ GenerateAggregateReports, GenerateDisclosureReports }
import hmda.persistence.messages.events.pubsub.PubSubEvents._
import hmda.persistence.model.serialization.PubSubEvents._
import hmda.persistence.serialization.pubsub.PubSubEventsProtobufConverter._

class PubSubEventsProtobufSerializer extends SerializerWithStringManifest {
  override def identifier: Int = 1010

  override def manifest(o: AnyRef) = o.getClass.getName

  final val SubmissionSignedPubSubManifest = classOf[SubmissionSignedPubSub].getName
  final val FindAggregatePublisherManifest = classOf[FindAggregatePublisher].getName
  final val FindDisclosurePublisherManifest = classOf[FindDisclosurePublisher].getName
  final val GenerateAggregateReportsManifest = classOf[GenerateAggregateReports].getName
  final val GenerateDisclosureReportsManifest = classOf[GenerateDisclosureReports].getName

  override def toBinary(o: AnyRef) = o match {
    case evt: SubmissionSignedPubSub => submissionSignedPubSubToProtobuf(evt).toByteArray
    case evt: FindAggregatePublisher => findAggregatePublisherToProtobuf(evt).toByteArray
    case evt: FindDisclosurePublisher => findDisclosurePublisherToProtobuf(evt).toByteArray
    case evt: GenerateAggregateReports => generateAggregateReportsToProtobuf(evt).toByteArray
    case evt: GenerateDisclosureReports => generateDisclosureReportsToProtobuf(evt).toByteArray
    case msg: Any => throw new RuntimeException(s"Cannot serialize this message: ${msg.toString}")
  }

  override def fromBinary(bytes: Array[Byte], manifest: String) = manifest match {
    case SubmissionSignedPubSubManifest =>
      submissionSignedPubSubFromProtobuf(SubmissionSignedPubSubMessage.parseFrom(bytes))
    case FindAggregatePublisherManifest =>
      findAggregatePublisherFromProtobuf(FindAggregatePublisherMessage.parseFrom(bytes))
    case FindDisclosurePublisherManifest =>
      findDisclosurePublisherFromProtobuf(FindDisclosurePublisherMessage.parseFrom(bytes))
    case GenerateAggregateReportsManifest =>
      generateAggregateReportsFromProtobuf(GenerateAggregateReportsMessage.parseFrom(bytes))
    case GenerateDisclosureReportsManifest =>
      generateDisclosureReportsFromProtobuf(GenerateDisclosureReportsMessage.parseFrom(bytes))
  }
}
