package hmda.persistence.serialization.pubsub

import akka.serialization.SerializerWithStringManifest
import hmda.persistence.messages.events.pubsub.PubSubEvents.SubmissionSignedPubSub
import hmda.persistence.model.serialization.PubSubEvents.SubmissionSignedPubSubMessage
import hmda.persistence.serialization.pubsub.PubSubEventsProtobufConverter._

class PubSubEventsProtobufSerializer extends SerializerWithStringManifest {
  override def identifier: Int = 1010

  override def manifest(o: AnyRef) = o.getClass.getName

  final val SubmissionSignedPubSubManifest = classOf[SubmissionSignedPubSub].getName

  override def toBinary(o: AnyRef) = o match {
    case evt: SubmissionSignedPubSub => submissionSignedPubSubToProtobuf(evt).toByteArray
    case msg: Any => throw new RuntimeException(s"Cannot serialize this message: ${msg.toString}")
  }

  override def fromBinary(bytes: Array[Byte], manifest: String) = manifest match {
    case SubmissionSignedPubSubManifest =>
      submissionSignedPubSubFromProtobuf(SubmissionSignedPubSubMessage.parseFrom(bytes))
  }
}
