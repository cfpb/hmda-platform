package hmda.serialization.institution

import java.io.NotSerializableException

import akka.serialization.SerializerWithStringManifest
import hmda.model.institution.InstitutionDetail
import hmda.persistence.serialization.institution.institutiondetail.InstitutionDetailMessage
import hmda.serialization.institution.InstitutionDetailProtobufConverter._
// $COVERAGE-OFF$
class InstitutionDetailSerializer extends SerializerWithStringManifest {
  override def identifier: Int = 110

  final val InstitutionDetailManifest = classOf[InstitutionDetail].getName

  override def manifest(o: AnyRef): String = o.getClass.getName

  override def toBinary(o: AnyRef): Array[Byte] = o match {
    case evt: InstitutionDetail =>
      institutionDetailToProtobuf(evt).toByteArray
    case _ =>
      throw new IllegalArgumentException(s"Cannot serialize object of type [${o.getClass.getName}]")
  }

  override def fromBinary(bytes: Array[Byte], manifest: String): AnyRef =
    manifest match {
      case InstitutionDetailManifest =>
        institutionDetailFromProtobuf(InstitutionDetailMessage.parseFrom(bytes))
      case _ =>
        throw new NotSerializableException(s"Unimplemented deserialization of message with manifest [$manifest] in [${getClass.getName}]")
    }

}
// $COVERAGE-OFF$