package hmda.serialization.submission

import hmda.messages.submission.HmdaRawDataReplies.LinesAdded
import org.scalatest.{MustMatchers, PropSpec}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import hmda.model.submission.LineAddedGenerator._
import org.scalacheck.Gen

class HmdaRawDataRepliesSerializerSpec extends PropSpec with ScalaCheckPropertyChecks with MustMatchers {
  val serializer = new HmdaRawDataRepliesSerializer()

  property("LinesAdded must serialize to and from binary") {
    forAll(Gen.listOf(lineAddedGen)) { linesAdded =>
      val reply = LinesAdded(linesAdded)
      val bytesCreated = serializer.toBinary(reply)
      serializer.fromBinary(bytesCreated, serializer.LinesAddedManifest) mustBe reply
    }
  }
}