package hmda.serialization.submission

import hmda.messages.submission.SubmissionEvents.{ SubmissionModified, SubmissionNotExists }
import hmda.messages.submission.SubmissionManagerCommands.{ UpdateSubmissionStatus, WrappedSubmissionEventResponse }
import hmda.model.submission.SubmissionGenerator._
import hmda.serialization.submission.SubmissionManagerCommandsProtobufConverter._
import org.scalatest.{ MustMatchers, PropSpec }
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

class SubmissionManagerCommandsProtobufConverterSpec extends PropSpec with ScalaCheckPropertyChecks with MustMatchers {

  property("UpdateSubmissionStatus must convert to and from protobuf") {
    forAll(submissionGen) { submission =>
      val created  = UpdateSubmissionStatus(submission)
      val protobuf = updateSubmissionStatusToProtobuf(created).toByteArray
      updateSubmissionStatusFromProtobuf(protobuf) mustBe created
    }
  }

  property("WrappedSubmissionEventResponse must convert to and from protobuf") {
    forAll(submissionGen) { submission =>
      val modified =
        WrappedSubmissionEventResponse(SubmissionModified(submission))
      val protobuf1 =
        wrappedSubmissionEventResponseToProtobuf(modified).toByteArray
      wrappedSubmissionEventResponseFromProtobuf(protobuf1) mustBe modified

      val notExists =
        WrappedSubmissionEventResponse(SubmissionNotExists(submission.id))
      val protobuf2 =
        wrappedSubmissionEventResponseToProtobuf(notExists).toByteArray
      wrappedSubmissionEventResponseFromProtobuf(protobuf2) mustBe notExists
    }
  }
}