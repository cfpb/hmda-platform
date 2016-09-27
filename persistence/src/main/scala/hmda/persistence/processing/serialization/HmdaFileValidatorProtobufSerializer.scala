package hmda.persistence.processing.serialization

import akka.serialization.SerializerWithStringManifest
import hmda.persistence.processing.HmdaFileValidator.{LarValidated, TsValidated}
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

  override def toBinary(o: AnyRef): Array[Byte] = ???

  override def fromBinary(bytes: Array[Byte], manifest: String): AnyRef = ???
}
