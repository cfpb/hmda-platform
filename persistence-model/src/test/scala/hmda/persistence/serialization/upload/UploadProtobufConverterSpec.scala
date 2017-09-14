package hmda.persistence.serialization.upload

import hmda.persistence.messages.events.processing.FileUploadEvents.LineAdded
import hmda.persistence.model.serialization.FileUpload.LineAddedMessage
import org.scalacheck.Gen
import org.scalatest.{ MustMatchers, PropSpec }
import org.scalatest.prop.PropertyChecks
import hmda.persistence.serialization.upload.UploadProtobufConverter._

class UploadProtobufConverterSpec extends PropSpec with PropertyChecks with MustMatchers {

  property("Line Added must serialize to protobuf and back") {
    forAll(Gen.calendar.sample.map(x => x.getTimeInMillis).getOrElse(0L), Gen.alphaStr) { (timestamp, data) =>
      val lineAdded = LineAdded(timestamp, data)
      val protobuf = lineAddedToProtobuf(lineAdded).toByteArray
      lineAddedFromProtobuf(LineAddedMessage.parseFrom(protobuf)) mustBe lineAdded
    }
  }

}
