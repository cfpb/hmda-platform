package hmda.persistence.serialization.parser

import akka.serialization.SerializerWithStringManifest
import hmda.persistence.messages.events.processing.HmdaFileParserEvents._
import hmda.persistence.model.serialization.HmdaFileParserEvents._
import hmda.persistence.serialization.parser.HmdaFileParserProtobufConverter._

class HmdaFileParserProtobufSerializer extends SerializerWithStringManifest {
  override def identifier: Int = 1003

  override def manifest(o: AnyRef): String = o.getClass.getName

  final val TsParsedManifest = classOf[TsParsed].getName
  final val TsParsedErrorsManifest = classOf[TsParsedErrors].getName
  final val LarParsedManifest = classOf[LarParsed].getName
  final val LarParsedErrorsManifest = classOf[LarParsedErrors].getName

  override def toBinary(o: AnyRef): Array[Byte] = o match {
    case event: TsParsed => tsParsedToProtobuf(event).toByteArray
    case event: TsParsedErrors => tsParsedErrorsToProtobuf(event).toByteArray
    case event: LarParsed => larParsedToProtobuf(event).toByteArray
    case event: LarParsedErrors => larParsedErrorsToProtobuf(event).toByteArray
    case msg: Any => throw new RuntimeException(s"Cannot serialize message: $msg")
  }

  override def fromBinary(bytes: Array[Byte], manifest: String): AnyRef = manifest match {
    case TsParsedManifest => tsParsedFromProtobuf(TsParsedMessage.parseFrom(bytes))
    case TsParsedErrorsManifest => tsParsedErrorsFromProtobuf(TsParsedErrorsMessage.parseFrom(bytes))
    case LarParsedManifest => larParsedFromProtobuf(LarParsedMessage.parseFrom(bytes))
    case LarParsedErrorsManifest => larParsedErrorsFromProtobuf(LarParsedErrorsMessage.parseFrom(bytes))
    case msg: Any => throw new RuntimeException(s"Cannot deserialize message: $msg")
  }
}
