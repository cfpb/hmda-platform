package hmda.serialization.filing

import java.io.NotSerializableException

import akka.serialization.SerializerWithStringManifest
import hmda.model.filing.FilingDetails
import hmda.persistence.serialization.filing.filingdetails.FilingDetailsMessage
import hmda.serialization.filing.FilingDetailsProtobufConverter._

class FilingDetailsSerializer extends SerializerWithStringManifest {
  override def identifier: Int = 107

  final val FilingDetailsManifest = classOf[FilingDetails].getName

  override def manifest(o: AnyRef): String = o.getClass.getName

  override def toBinary(o: AnyRef): Array[Byte] = o match {
    case evt: FilingDetails =>
      filingDetailsToProtobuf(evt).toByteArray
    case _ =>
      throw new IllegalArgumentException(
        s"Cannot serialize object of type [${o.getClass.getName}]")

  }

  override def fromBinary(bytes: Array[Byte], manifest: String): AnyRef =
    manifest match {
      case FilingDetailsManifest =>
        filingDetailsFromProtobuf(FilingDetailsMessage.parseFrom(bytes))
      case _ =>
        throw new NotSerializableException(
          s"Unimplemented deserialization of message with manifest [$manifest] in [${getClass.getName}]")

    }
}
