package hmda.persistence.institutions.serialization

import akka.serialization.SerializerWithStringManifest
import hmda.persistence.institutions.FilingPersistence.{ FilingCreated, FilingStatusUpdated }
import hmda.persistence.messages.{ FilingCreatedMessage, FilingStatusUpdatedMessage }
import hmda.persistence.institutions.serialization.FilingConverter._

class FilingPersistenceProtobufSerializer extends SerializerWithStringManifest {
  override def identifier: Int = 8001

  override def manifest(o: AnyRef): String = o.getClass.getName

  final val FilingCreatedManifest = classOf[FilingCreated].getName
  final val FilingStatusUpdatedManifest = classOf[FilingStatusUpdated].getName

  override def fromBinary(bytes: Array[Byte], manifest: String): AnyRef = manifest match {
    case FilingCreatedManifest =>
      val filingCreatedMessage = FilingCreatedMessage.parseFrom(bytes)
      FilingCreated(filingCreatedMessage.filing)

    case FilingStatusUpdatedManifest =>
      val filingStatusUpdatedMessage = FilingStatusUpdatedMessage.parseFrom(bytes)
      FilingStatusUpdated(filingStatusUpdatedMessage.filing)
  }

  override def toBinary(o: AnyRef): Array[Byte] = o match {
    case FilingCreated(filing) =>
      FilingCreatedMessage(filing).toByteArray
    case FilingStatusUpdated(filing) =>
      FilingStatusUpdatedMessage(filing).toByteArray
  }

}
