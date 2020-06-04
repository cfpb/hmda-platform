package hmda.serialization.filing

import java.io.NotSerializableException

import akka.serialization.SerializerWithStringManifest
import hmda.model.filing.FilingDetails
import hmda.persistence.filing.FilingState
import hmda.persistence.serialization.filing.filingdetails.FilingDetailsMessage
import hmda.persistence.serialization.filing.filingstate.FilingStateMessage
import hmda.serialization.filing.FilingDetailsProtobufConverter._
import hmda.serialization.filing.FilingStateProtobufConverter._
// $COVERAGE-OFF$
class FilingDetailsSerializer extends SerializerWithStringManifest {
  override def identifier: Int = 107

  final val FilingDetailsManifest = classOf[FilingDetails].getName

  final val FilingStateManifest = classOf[FilingState].getName

  override def manifest(o: AnyRef): String = o.getClass.getName

  override def toBinary(o: AnyRef): Array[Byte] = o match {
    case evt: FilingDetails =>
      filingDetailsToProtobuf(evt).toByteArray
    case _ =>
      throw new IllegalArgumentException(s"Cannot serialize object of type [${o.getClass.getName}]")

  }

  override def fromBinary(bytes: Array[Byte], manifest: String): AnyRef =
    manifest match {
      case FilingDetailsManifest =>
        filingDetailsFromProtobuf(FilingDetailsMessage.parseFrom(bytes))
      case FilingStateManifest =>
        filingStateFromProtobuf(FilingStateMessage.parseFrom(bytes))
      case _ =>
        throw new NotSerializableException(s"Unimplemented deserialization of message with manifest [$manifest] in [${getClass.getName}]")

    }
}
// $COVERAGE-ON$
