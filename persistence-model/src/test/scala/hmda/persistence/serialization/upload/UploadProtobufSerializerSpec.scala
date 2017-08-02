package hmda.persistence.serialization.upload

import hmda.persistence.messages.events.processing.FileUploadEvents.{ FileNameAdded, LineAdded }
import org.scalacheck.Gen
import org.scalatest.{ MustMatchers, PropSpec }
import org.scalatest.prop.PropertyChecks

class UploadProtobufSerializerSpec extends PropSpec with PropertyChecks with MustMatchers {
  val serializer = new UploadProtobufSerializer()

  property("Line Added messages must be serialized to binary and back") {
    forAll(Gen.calendar.sample.map(x => x.getTimeInMillis).getOrElse(0L), Gen.alphaStr) { (timestamp, data) =>
      val msg = LineAdded(timestamp, data)
      val bytes = serializer.toBinary(msg)
      serializer.fromBinary(bytes, serializer.LineAddedManifest) mustBe msg
    }
  }

  property("File Name Added messages must be serialized to binary and back") {
    forAll(Gen.alphaStr) { fileName =>
      val msg = FileNameAdded(fileName)
      val bytes = serializer.toBinary(msg)
      serializer.fromBinary(bytes, serializer.FileNameAddedManifest) mustBe msg
    }
  }
}
