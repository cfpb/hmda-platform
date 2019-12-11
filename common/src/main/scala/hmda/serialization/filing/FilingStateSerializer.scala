package hmda.serialization.filing

import java.io.NotSerializableException

import akka.serialization.SerializerWithStringManifest
import hmda.persistence.filing.FilingState
import hmda.persistence.serialization.filing.filingstate.FilingStateMessage
import hmda.serialization.filing.FilingStateProtobufConverter._

class FilingStateSerializer extends SerializerWithStringManifest {
  override def identifier: Int = 1020

  final val FilingStateManifest = classOf[FilingState].getName

  override def manifest(o: AnyRef): String = o.getClass.getName

  override def toBinary(o: AnyRef): Array[Byte] = o match {
    case evt: FilingState =>
      filingStateToProtobuf(evt).toByteArray
    case _ =>
      throw new IllegalArgumentException(s"Cannot serialize object of type [${o.getClass.getName}]")

  }

  override def fromBinary(bytes: Array[Byte], manifest: String): AnyRef =
    manifest match {
      case FilingStateManifest =>
        filingStateFromProtobuf(FilingStateMessage.parseFrom(bytes))
      case _ =>
        throw new NotSerializableException(s"Unimplemented deserialization of message with manifest [$manifest] in [${getClass.getName}]")

    }
}
