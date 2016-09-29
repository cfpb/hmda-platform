package hmda.persistence.processing.serialization

import akka.serialization.SerializerWithStringManifest
import hmda.persistence.processing.HmdaFileValidator.{ LarValidated, TsValidated }
import hmda.persistence.messages.{ LarValidatedMessage, TsValidatedMessage }
import hmda.validation.engine.ValidationError

class HmdaFileValidatorProtobufSerializer
    extends SerializerWithStringManifest
    with TsMessageConverter
    with LarMessageConverter {

  override def identifier: Int = 9003

  override def manifest(o: AnyRef): String = o.getClass.getName

  final val TsValidatedManifest = classOf[TsValidated].getName
  final val LarValidatedManifest = classOf[LarValidated].getName
  final val ValidationErrorManifest = classOf[ValidationError].getName

  override def fromBinary(bytes: Array[Byte], manifest: String): AnyRef = manifest match {
    case TsValidatedManifest =>
      val tsValidatedMessage = TsValidatedMessage.parseFrom(bytes)
      for {
        t <- tsValidatedMessage.ts
        ts = messageToTransmittalSheet(t)
      } yield ts

    case LarValidatedManifest =>
      val larValidatedMessage = LarValidatedMessage.parseFrom(bytes)
      for {
        l <- larValidatedMessage.lar
        lar = messageToLoanApplicationRegister(l)
      } yield lar
  }

  override def toBinary(o: AnyRef): Array[Byte] = o match {
    case TsValidated(ts) =>
      TsValidatedMessage(transmittalSheetToMessage(ts)).toByteArray

    case LarValidated(lar) =>
      LarValidatedMessage(loanApplicationRegisterToMessage(lar)).toByteArray
  }

}
