package hmda.persistence.serialization.pubsub

import org.scalatest.{ MustMatchers, PropSpec }
import org.scalatest.prop.PropertyChecks
import hmda.model.institution.SubmissionGenerators._
import hmda.persistence.messages.events.pubsub.PubSubEvents.SubmissionSignedPubSub
import hmda.persistence.model.serialization.PubSubEvents.SubmissionSignedPubSubMessage
import hmda.persistence.serialization.pubsub.PubSubEventsProtobufConverter._

class PubSubEventsProtobufConverterSpec extends PropSpec with PropertyChecks with MustMatchers {

  property("Submission Signed pubsub event must serialize to protobuf and back") {
    forAll(submissionIdGen) { submissionId =>
      val submissionSigned = SubmissionSignedPubSub(submissionId)
      val protobuf = submissionSignedPubSubToProtobuf(submissionSigned).toByteArray
      submissionSignedPubSubFromProtobuf(SubmissionSignedPubSubMessage.parseFrom(protobuf)) mustBe SubmissionSignedPubSub(submissionId)
    }
  }
}
