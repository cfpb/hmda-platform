package hmda.persistence.institutions.serialization

import akka.serialization.SerializerWithStringManifest

class SubmissionPersistenceProtobufSerializer extends SerializerWithStringManifest {
  override def identifier: Int = ???

  override def manifest(o: AnyRef): String = ???

  override def toBinary(o: AnyRef): Array[Byte] = ???

  override def fromBinary(bytes: Array[Byte], manifest: String): AnyRef = ???
}
