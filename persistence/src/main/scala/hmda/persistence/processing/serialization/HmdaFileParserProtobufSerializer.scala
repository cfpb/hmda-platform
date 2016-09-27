package hmda.persistence.processing.serialization

import akka.serialization.SerializerWithStringManifest
import hmda.persistence.messages.{ LarParsedErrorsMessage, LarParsedMessage, TsParsedErrorsMessage, TsParsedMessage }
import hmda.persistence.processing.HmdaFileParser.{ LarParsed, LarParsedErrors, TsParsed, TsParsedErrors }

class HmdaFileParserProtobufSerializer
    extends SerializerWithStringManifest
    with TsMessageConverter
    with LarMessageConverter {

  override def identifier: Int = 9002

  override def manifest(o: AnyRef): String = o.getClass.getName

  final val TsParsedManifest = classOf[TsParsed].getName
  final val TsParsedErrorsManifest = classOf[TsParsedErrors].getName
  final val LarParsedManifest = classOf[LarParsed].getName
  final val LarParsedErrorsManifest = classOf[LarParsedErrors].getName

  override def fromBinary(bytes: Array[Byte], manifest: String): AnyRef = manifest match {
    case TsParsedManifest =>
      val tsParsedMessage = TsParsedMessage.parseFrom(bytes)
      for {
        t <- tsParsedMessage.ts
        ts = messageToTransmittalSheet(t)
      } yield ts

    case TsParsedErrorsManifest =>
      val tsParsedErrorsMessage = TsParsedErrorsMessage.parseFrom(bytes)
      TsParsedErrors(tsParsedErrorsMessage.errors.toList)

    case LarParsedManifest =>
      val larParsedMessage = LarParsedMessage.parseFrom(bytes)
      for {
        l <- larParsedMessage.lar
        lar = messageToLoanApplicationRegister(l)
      } yield lar

    case LarParsedErrorsManifest =>
      val larParsedErrorsMessage = LarParsedErrorsMessage.parseFrom(bytes)
      LarParsedErrors(larParsedErrorsMessage.errors.toList)
  }

  override def toBinary(o: AnyRef): Array[Byte] = o match {
    case TsParsed(ts) =>
      TsParsedMessage(transmittalSheetToMessage(ts)).toByteArray

    case TsParsedErrors(errors) =>
      TsParsedErrorsMessage(errors).toByteArray

    case LarParsed(lar) =>
      LarParsedMessage(loanApplicationRegisterToMessage(lar)).toByteArray

    case LarParsedErrors(errors) =>
      LarParsedErrorsMessage(errors).toByteArray
  }

}
