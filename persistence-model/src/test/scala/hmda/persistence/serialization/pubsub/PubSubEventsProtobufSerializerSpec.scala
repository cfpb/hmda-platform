package hmda.persistence.serialization.pubsub

import org.scalatest.{ MustMatchers, PropSpec }
import org.scalatest.prop.PropertyChecks
import hmda.model.institution.SubmissionGenerators._
import hmda.persistence.messages.events.pubsub.PubSubEvents.SubmissionSignedPubSub

class PubSubEventsProtobufSerializerSpec extends PropSpec with PropertyChecks with MustMatchers {

  val serializer = new PubSubEventsProtobufSerializer()

  property("Submission Signed PubSub messages must be serialized to binary and back") {
    forAll(submissionIdGen) { submissionId =>
      val msg = SubmissionSignedPubSub(submissionId)
      val protobuf = serializer.toBinary(msg)
      serializer.fromBinary(protobuf, serializer.SubmissionSignedPubSubManifest) mustBe msg
    }
  }
}
