package hmda.query.serialization

import akka.serialization.SerializerWithStringManifest
import hmda.persistence.model.serialization.InstitutionViewEvents.InstitutionViewStateMessage
import hmda.query.view.institutions.InstitutionView.InstitutionViewState
import hmda.query.serialization.InstitutionViewProtobufConverter._

class InstitutionViewProtobufSerializer extends SerializerWithStringManifest {
  override def identifier: Int = 1011

  override def manifest(o: AnyRef): String = o.getClass.getName

  final val InstitutionViewStateManifest = classOf[InstitutionViewState].getName

  override def toBinary(o: AnyRef): Array[Byte] = o match {
    case state: InstitutionViewState => institutionViewStateToProtobuf(state).toByteArray
    case msg: Any => throw new RuntimeException(s"Cannot serialize this message: ${msg.toString}")
  }

  override def fromBinary(bytes: Array[Byte], manifest: String): AnyRef = manifest match {
    case InstitutionViewStateManifest =>
      institutionViewStateFromProtobuf(InstitutionViewStateMessage.parseFrom(bytes))
    case msg: Any => throw new RuntimeException(s"Cannot deserialize this message: ${msg.toString}")
  }
}
