package hmda.persistence.serialization.processing

import akka.serialization.SerializerWithStringManifest
import hmda.persistence.messages.commands.processing.HmdaFileValidatorState.{ HmdaVerificationState, QMState, SVState }
import HmdaFileValidatorStateProtobufConverter._
import hmda.persistence.model.serialization.HmdaFileValidatorCommands.{ HmdaVerificationStateMessage, QMStateMessage, SVStateMessage }

class HmdaFileValidatorStateProtobufSerializer extends SerializerWithStringManifest {
  override def identifier: Int = 1011

  override def manifest(o: AnyRef): String = o.getClass.getName

  final val SVStateManifest = classOf[SVState].getName
  final val QMStateManifest = classOf[QMState].getName
  final val VerificationManifest = classOf[HmdaVerificationState].getName

  override def toBinary(o: AnyRef): Array[Byte] = o match {
    case s: SVState => svStateToProtobuf(s).toByteArray
    case s: QMState => qmStateToProtobuf(s).toByteArray
    case s: HmdaVerificationState => verificationStateToProtobuf(s).toByteArray
    case msg: Any => throw new RuntimeException(s"Cannot serialize this message: ${msg.toString}")
  }

  override def fromBinary(bytes: Array[Byte], manifest: String): AnyRef = manifest match {
    case SVStateManifest => svStateFromProtobuf(SVStateMessage.parseFrom(bytes))
    case QMStateManifest => qmStateFromProtobuf(QMStateMessage.parseFrom(bytes))
    case VerificationManifest => verificationStateFromProtobuf(HmdaVerificationStateMessage.parseFrom(bytes))
  }
}
