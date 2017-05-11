package hmda.persistence.serialization.filing

import akka.serialization.SerializerWithStringManifest

class HmdaFilingProtobufSerializer extends SerializerWithStringManifest {
  override def identifier: Int = 1004

  override def manifest(o: AnyRef): String = o.getClass.getName

  override def toBinary(o: AnyRef): Array[Byte] = ???

  override def fromBinary(bytes: Array[Byte], manifest: String): AnyRef = ???
}
