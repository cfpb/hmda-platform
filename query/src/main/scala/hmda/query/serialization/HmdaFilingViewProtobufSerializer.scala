package hmda.query.serialization

import akka.serialization.SerializerWithStringManifest
import hmda.persistence.model.serialization.HmdaFilingViewState.FilingViewStateMessage
import hmda.query.view.filing.HmdaFilingView.FilingViewState
import hmda.query.serialization.HmdaFilingViewProtobufConverter._

class HmdaFilingViewProtobufSerializer extends SerializerWithStringManifest {
  override def identifier: Int = 1012

  override def manifest(o: AnyRef): String = o.getClass.getName

  final val FilingViewStateManifest = classOf[FilingViewState].getName

  override def toBinary(o: AnyRef): Array[Byte] = o match {
    case state: FilingViewState => filingViewStateToProtobuf(state).toByteArray
    case msg: Any => throw new RuntimeException(s"Cannot serialize this message: ${msg.toString}")
  }

  override def fromBinary(bytes: Array[Byte], manifest: String): AnyRef = manifest match {
    case FilingViewStateManifest =>
      filingViewStateFromProtobuf(FilingViewStateMessage.parseFrom(bytes))
    case msg: Any => throw new RuntimeException(s"Cannot deserialize this message: ${msg.toString}")
  }

}
