package hmda.persistence.processing.serialization

import akka.serialization.SerializerWithStringManifest
import hmda.model.fi.lar.LoanApplicationRegister
import hmda.model.fi.ts.TransmittalSheet
import hmda.persistence.messages.{ LarParsedErrorsMessage, LarParsedMessage, TsParsedErrorsMessage, TsParsedMessage }
import hmda.persistence.processing.HmdaFileParser.{ LarParsed, LarParsedErrors, TsParsed, TsParsedErrors }
import hmda.persistence.processing.serialization.TsConverter._
import hmda.persistence.processing.serialization.LarConverter._

class HmdaFileParserProtobufSerializer extends SerializerWithStringManifest {

  override def identifier: Int = 9002

  override def manifest(o: AnyRef): String = o.getClass.getName

  final val TsParsedManifest = classOf[TsParsed].getName
  final val TsParsedErrorsManifest = classOf[TsParsedErrors].getName
  final val LarParsedManifest = classOf[LarParsed].getName
  final val LarParsedErrorsManifest = classOf[LarParsedErrors].getName

  override def fromBinary(bytes: Array[Byte], manifest: String): AnyRef = manifest match {
    case TsParsedManifest =>
      TsParsedMessage.parseFrom(bytes)

    case TsParsedErrorsManifest =>
      val tsParsedErrorsMessage = TsParsedErrorsMessage.parseFrom(bytes)
      TsParsedErrors(tsParsedErrorsMessage.errors.toList)

    case LarParsedManifest =>
      LarParsedMessage.parseFrom(bytes)

    case LarParsedErrorsManifest =>
      val larParsedErrorsMessage = LarParsedErrorsMessage.parseFrom(bytes)
      LarParsedErrors(larParsedErrorsMessage.errors.toList)
  }

  override def toBinary(o: AnyRef): Array[Byte] = o match {
    case TsParsed(ts) =>
      TsParsedMessage(ts).toByteArray

    case TsParsedErrors(errors) =>
      TsParsedErrorsMessage(errors).toByteArray

    case LarParsed(lar) =>
      LarParsedMessage(lar).toByteArray

    case LarParsedErrors(errors) =>
      LarParsedErrorsMessage(errors).toByteArray
  }

}
