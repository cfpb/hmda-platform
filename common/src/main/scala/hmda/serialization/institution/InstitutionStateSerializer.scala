package hmda.serialization.institution

import java.io.NotSerializableException

import akka.serialization.SerializerWithStringManifest
import hmda.persistence.institution.InstitutionState
import hmda.persistence.serialization.institution.institutionstate.InstitutionStateMessage
import hmda.serialization.institution.InstitutionStateProtobufConverter._
// $COVERAGE-OFF$
class InstitutionStateSerializer extends SerializerWithStringManifest {
  override def identifier: Int = 120

  final val InstitutionStateManifest = classOf[InstitutionState].getName

  override def manifest(o: AnyRef): String = o.getClass.getName

  override def toBinary(o: AnyRef): Array[Byte] = o match {
    case evt: InstitutionState =>
      institutionStateToProtobuf(evt).toByteArray
    case _ =>
      throw new IllegalArgumentException(s"Cannot serialize object of type [${o.getClass.getName}]")
  }

  override def fromBinary(bytes: Array[Byte], manifest: String): AnyRef =
    manifest match {
      case InstitutionStateManifest =>
        institutionStateFromProtobuf(InstitutionStateMessage.parseFrom(bytes))
      case _ =>
        throw new NotSerializableException(s"Unimplemented deserialization of message with manifest [$manifest] in [${getClass.getName}]")
    }

}
// $COVERAGE-OFF$