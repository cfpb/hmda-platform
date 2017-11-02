package hmda.persistence.serialization.filing

import akka.serialization.SerializerWithStringManifest
import hmda.model.fi.Filing
import hmda.persistence.messages.events.institutions.FilingEvents.{ FilingCreated, FilingStatusUpdated }
import hmda.persistence.model.serialization.FilingEvents.{ FilingCreatedMessage, FilingMessage, FilingStatusUpdatedMessage }
import hmda.persistence.serialization.filing.FilingProtobufConverter._

class FilingProtobufSerializer extends SerializerWithStringManifest {
  override def identifier: Int = 1002

  override def manifest(o: AnyRef): String = o.getClass.getName

  final val FilingCreatedManifest = classOf[FilingCreated].getName
  final val FilingStatusUpdatedManifest = classOf[FilingStatusUpdated].getName
  final val FilingManifest = classOf[Filing].getName

  override def toBinary(o: AnyRef): Array[Byte] = o match {
    case evt: FilingCreated => filingCreatedToProtobuf(evt).toByteArray
    case evt: FilingStatusUpdated => filingStatusUpdatedToProtobuf(evt).toByteArray
    case evt: Filing => filingToProtobuf(evt).toByteArray
    case msg: Any => throw new RuntimeException(s"Cannot serialize this message: ${msg.toString}")
  }

  override def fromBinary(bytes: Array[Byte], manifest: String): AnyRef = manifest match {
    case FilingCreatedManifest =>
      filingCreatedFromProtobuf(FilingCreatedMessage.parseFrom(bytes))
    case FilingStatusUpdatedManifest =>
      filingStatusUpdatedFromProtobuf(FilingStatusUpdatedMessage.parseFrom(bytes))
    case FilingManifest =>
      filingFromProtobuf(FilingMessage.parseFrom(bytes))
    case msg: Any => throw new RuntimeException(s"Cannot deserialize this message: ${msg.toString}")
  }
}
