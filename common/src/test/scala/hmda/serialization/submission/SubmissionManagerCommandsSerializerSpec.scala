package hmda.serialization.submission

import hmda.messages.submission.SubmissionEvents.{ SubmissionCreated, SubmissionNotExists }
import hmda.messages.submission.SubmissionManagerCommands.{ UpdateSubmissionStatus, WrappedSubmissionEventResponse }
import hmda.model.submission.SubmissionGenerator._
import org.scalatest.{ MustMatchers, PropSpec }
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

class SubmissionManagerCommandsSerializerSpec extends PropSpec with ScalaCheckPropertyChecks with MustMatchers {

  val serializer = new SubmissionManagerCommandsSerializer()

  property("UpdateSubmissionStatus must serialize to and from binary") {
    forAll(submissionGen) { submission =>
      val update       = UpdateSubmissionStatus(submission)
      val bytesCreated = serializer.toBinary(update)
      serializer.fromBinary(bytesCreated, serializer.UpdateSubmissionStatusManifest) mustBe update
    }
  }

  property("WrappedSubmissionEventResponse must serialize to and from binary") {
    forAll(submissionGen) { submission =>
      val created =
        WrappedSubmissionEventResponse(SubmissionCreated(submission))
      val bytesCreated = serializer.toBinary(created)
      serializer.fromBinary(bytesCreated, serializer.WrappedSubmissionEventResponseManifest) mustBe created

      val notExists =
        WrappedSubmissionEventResponse(SubmissionNotExists(submission.id))
      val bytesNotExists = serializer.toBinary(notExists)
      serializer.fromBinary(bytesNotExists, serializer.WrappedSubmissionEventResponseManifest) mustBe notExists
    }
  }

}