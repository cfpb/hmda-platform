package hmda.serialization.submission

import java.io.NotSerializableException

import akka.serialization.SerializerWithStringManifest
import hmda.messages.submission.HmdaRawDataEvents.LineAdded
import hmda.model.processing.state.HmdaRawDataState
import hmda.persistence.serialization.raw.data.events.{ HmdaRawDataStateMessage, LineAddedMessage }
import hmda.serialization.submission.HmdaRawDataEventsProtobufConverter._

class HmdaRawDataEventsSerializer extends SerializerWithStringManifest {
  override def identifier: Int = 114

  final val LineAddedManifest        = classOf[LineAdded].getName
  final val HmdaRawDataStateManifest = classOf[HmdaRawDataState].getName

  override def manifest(o: AnyRef): String = o.getClass.getName

  override def toBinary(o: AnyRef): Array[Byte] = o match {
    case evt: LineAdded =>
      lineAddedToProtobuf(evt).toByteArray
    case evt: HmdaRawDataState =>
      rawDataStateToProtobuf(evt).toByteArray
    case _ =>
      throw new IllegalArgumentException(s"Cannot serialize object of type [${o.getClass.getName}]")
  }

  override def fromBinary(bytes: Array[Byte], manifest: String): AnyRef =
    manifest match {
      case LineAddedManifest =>
        lineAddedFromProtobuf(LineAddedMessage.parseFrom(bytes))
      case HmdaRawDataStateManifest =>
        rawDataStateFromProtobuf(HmdaRawDataStateMessage.parseFrom(bytes))
      case _ =>
        throw new NotSerializableException(s"Unimplemented deserialization of message with manifest [$manifest] in [${getClass.getName}]")
    }
}
